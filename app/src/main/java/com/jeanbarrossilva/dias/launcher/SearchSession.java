package com.jeanbarrossilva.dias.launcher;

import android.icu.text.Normalizer2;

import androidx.annotation.Nullable;

import com.jeanbarrossilva.OneTimeCopyOnWriteArrayList;
import com.jeanbarrossilva.dias.launcher.installation.InstallationDiff;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;

/**
 * Session for searching for handles with text-based queries.
 *
 * @implNote The underlying implementation resorts to Apache Lucene, and doesn't
 *   store files on disk. Rather, the index lives on the heap, and is based on a
 *   byte buffer.
 */
public final class SearchSession {
  /**
   * Unique identifier of this session. Allows the launcher to index this
   * session in constant time, retrieving and ending it whenever the user
   * requests it to be ended or the launcher gets closed.
   *
   * @see Launcher#endSearchSession(SearchSession)
   * @see Launcher#close()
   */
  final UUID id;

  private final Directory directory;
  private final IndexWriter writer;
  private Handle[] handles;

  private static final String FIELD_ID = "id";
  private static final String FIELD_UNTOKENIZED_LABEL = "untokenized_label";
  private static final String FIELD_TOKENIZED_LABEL = "tokenized_label";
  private static final String FIELD_POSITION = "position";

  SearchSession() {
    try {
      final var writerConfig = new IndexWriterConfig();
      writerConfig.setCommitOnClose(false);
      this.id = randomUUID();
      this.directory = new ByteBuffersDirectory();
      this.writer = new IndexWriter(directory, writerConfig);
      this.writer.commit();
      this.handles = null;
    } catch (final IOException cause) {
      throw new RuntimeException(cause);
    }
  }

  /**
   * Finds the handles that match the given query.
   *
   * @param query Text to search handles by. This query will be matched against
   *   the label of every handle, and can be part of that label (e.g., for a
   *   handle labeled "Calculator", {@code query} being {@code "calc"} would
   *   include the handle in the returned array), match it exactly or be written
   *   with a typo.
   * @return The search results, or {@code null} if the query is {@code null}.
   * @see Handle#label
   */
  @Nullable
  public List<Handle> search(@Nullable final String query) {
    if (query == null)
      return null;
    if (query.isBlank())
      return new OneTimeCopyOnWriteArrayList<>(
                                       handles,
        /* isImmutableTreeSetLike = */ true
      );
    try(final IndexReader reader = DirectoryReader.open(writer)) {
      final IndexSearcher searcher = new IndexSearcher(reader);
      final ScoreDoc[] scoreDocuments =
        searcher.search(query(query), Integer.MAX_VALUE).scoreDocs;
      final var result = new ArrayList<Handle>(scoreDocuments.length);
      for (final ScoreDoc scoreDocument: scoreDocuments)
        result.add(handles[reader.storedFields()
                                 .document(scoreDocument.doc)
                                 .getField(FIELD_POSITION)
                                 .numericValue()
                                 .intValue()]);
      return result;
    } catch (final IOException cause) {
      throw new RuntimeException(cause);
    }
  }

  /**
   * Synchronizes the index with the given handles.
   * <p>
   * This method should only be called immediately after this session is
   * instantiated; syncing this way might be expensive, as it wipes the entire
   * index (if populated) and iterates over each handle.
   * <p>
   * For syncing due to changes on handles that have already been synced, and
   * considering only the what's been changed on them, call
   * {@link #sync(InstallationDiff)} instead.
   *
   * @param handles All handles in the launcher.
   */
  void sync(final Handle[] handles) {
    try {
      this.handles = handles;
      writer.prepareCommit();
      writer.deleteAll();
      for (int position = 0; position < handles.length; position++)
        writer.addDocument(toDocument(handles[position], position));
      writer.commit();
    } catch (final IOException cause) {
      throw new RuntimeException(cause);
    }
  }

  /**
   * Synchronizes the index according to the given diff. Instead of taking an
   * entire array of handles into account (as does {@link #sync(Handle[])}),
   * only the change to the handles given to this session last will be synced.
   *
   * @param diff Change to the handles provided to the last call to
   *   {@link #sync(Handle[])} to be synced with this session.
   */
  void sync(final InstallationDiff diff) {
    try {
      handles = diff.merge(handles);
      writer.prepareCommit();
      switch (diff.kind) {
        case INSTALLATION:
          for (int j = 0; j < diff.handles.length; j++)
            writer.addDocument(toDocument(diff.handles[j], diff.indices[j]));
          break;
        case REPLACEMENT:
          for (int j = 0; j < diff.handles.length; j++)
            writer.updateDocument(
              toTerm(diff.handles[j]),
              toDocument(diff.handles[j], diff.indices[j])
            );
          break;
        case UNINSTALLATION:
          final var terms = new Term[diff.handles.length];
          for (int j = 0; j < terms.length; j++)
            terms[j] = toTerm(diff.handles[j]);
          writer.deleteDocuments(terms);
          break;
      }
      writer.commit();
    } catch (final IOException cause) {
      throw new RuntimeException(cause);
    }
  }

  void end() {
    try {
      writer.prepareCommit();
      writer.deleteAll();
      writer.commit();
      writer.close();
      directory.close();
    } catch (final IOException cause) {
      throw new RuntimeException(cause);
    }
  }

  private static Query query(final String text) {
    final var builder = new BooleanQuery.Builder();
    final String normalizedText = normalize(text);
    builder.add(
      new BoostQuery(
        new TermQuery(new Term(FIELD_UNTOKENIZED_LABEL, normalizedText)),
        1.0f
      ),
      BooleanClause.Occur.SHOULD
    );
    for (final String word: text.split(" ")) {
      if (word.isBlank())
        continue;
      final String escapedWord = word
        .replace("*", "\\*")
        .replace("?", "\\?")
        .replace("\\", "\\\\");
      builder.add(
        new WildcardQuery(
          new Term(FIELD_TOKENIZED_LABEL, '*' + escapedWord + '*')
        ),
        BooleanClause.Occur.SHOULD
      );
    }
    return builder.build();
  }

  private static Term toTerm(final Handle handle) {
    return new Term(FIELD_ID, handle.id);
  }

  private static Document toDocument(final Handle handle, final int position) {
    final var result = new Document();
    final String label = normalize(handle.label);
    for (final IndexableField field: new IndexableField[]{
      new StringField(FIELD_ID, handle.id, Field.Store.YES),
      new StringField(FIELD_UNTOKENIZED_LABEL, label, Field.Store.YES),
      new TextField(FIELD_TOKENIZED_LABEL, label, Field.Store.NO),
      new StoredField(FIELD_POSITION, position)
    })
      result.add(field);
    return result;
  }

  private static String normalize(final String text) {
    return Normalizer2.getNFKDInstance().normalize(text);
  }
}