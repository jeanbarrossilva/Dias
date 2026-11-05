package com.jeanbarrossilva.dias.launcher;

import static androidx.appsearch.app.AppSearchSchema.StringPropertyConfig.INDEXING_TYPE_PREFIXES;
import static com.jeanbarrossilva.dias.core.ListenableFutures.toCompletableFuture;
import static java.util.Arrays.stream;
import static java.util.UUID.randomUUID;

import android.content.Context;
import android.icu.text.Normalizer2;

import androidx.annotation.Nullable;
import androidx.appsearch.annotation.Document;
import androidx.appsearch.app.AppSearchSession;
import androidx.appsearch.app.PutDocumentsRequest;
import androidx.appsearch.app.RemoveByDocumentIdRequest;
import androidx.appsearch.app.SearchResult;
import androidx.appsearch.app.SearchResults;
import androidx.appsearch.app.SearchSpec;
import androidx.appsearch.app.SetSchemaRequest;
import androidx.appsearch.exceptions.AppSearchException;
import androidx.appsearch.localstorage.LocalStorage;

import com.google.common.util.concurrent.ListenableFuture;
import com.jeanbarrossilva.OneTimeCopyOnWriteArrayList;
import com.jeanbarrossilva.dias.launcher.installation.InstallationDiff;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.IntFunction;

/** Session for searching for handles with text-based queries. */
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

  private final ListenableFuture<AppSearchSession> delegateFuture;
  private Handle[] handles;

  private static final Runnable NO_OP = () -> {};
  private static final SearchSpec SPEC = new SearchSpec.Builder()
    .addFilterNamespaces(HandleDocument.NAMESPACE)
    .setResultCountPerPage(10000)
    .build();
  private static final SetSchemaRequest SET_SCHEMA_REQUEST;

  static {
    try {
      SET_SCHEMA_REQUEST = new SetSchemaRequest.Builder()
        .addDocumentClasses(HandleDocument.class)
        .build();
    } catch (final AppSearchException cause) {
      throw new RuntimeException(cause);
    }
  }

  @Document
  @SuppressWarnings("ClassCanBeRecord")
  static final class HandleDocument {
    @Document.Namespace
    public final String namespace;

    @Document.Id
    public final String id;

    @Document.StringProperty
    public final String exactLabel;

    @Document.StringProperty(indexingType = INDEXING_TYPE_PREFIXES)
    public final String normalizedLabel;

    private static final String NAMESPACE = "dias";

    public HandleDocument(final Handle handle) {
      this(NAMESPACE, handle.id, handle.label, normalize(handle.label));
    }

    public HandleDocument(
      final String namespace,
      final String id,
      final String exactLabel,
      final String normalizedLabel
    ) {
      this.namespace = namespace;
      this.id = id;
      this.exactLabel = exactLabel;
      this.normalizedLabel = normalizedLabel;
    }

    public Handle toHandle() {
      return new Handle(id, exactLabel);
    }

    public static String normalize(final String exactLabel) {
      return Normalizer2.getNFKCInstance()
        .normalize(exactLabel)
        .toLowerCase(Locale.ROOT);
    }
  }

  SearchSession(final Context context, final String indexName) {
    final LocalStorage.SearchContext searchContext =
      new LocalStorage.SearchContext.Builder(context, indexName).build();
    this.id = randomUUID();
    this.delegateFuture = LocalStorage.createSearchSessionAsync(searchContext);
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
    final String normalizedQuery = HandleDocument.normalize(query);
    final SearchResults resource = getDelegate().search(normalizedQuery, SPEC);
    final List<SearchResult> page = await(resource.getNextPageAsync());
    final List<Handle> result = page
      .stream()
      .map(searchResult -> {
        try {
          return searchResult
            .getGenericDocument()
            .toDocumentClass(HandleDocument.class)
            .toHandle();
        } catch (final AppSearchException cause) {
          throw new RuntimeException(cause);
        }
      })
        .toList();
    resource.close();
    return result;
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
   * {@link #sync(InstallationDiff, Handle[])} instead.
   *
   * @param handles All handles in the launcher.
   */
  void sync(final Handle[] handles) {
    final AppSearchSession delegate = getDelegate();
    clear(delegate);
    this.handles = handles;
    awaitAll(
      stream(handles)
        .map(handle -> putDocumentAsync(delegate, handle))
        .toArray(CompletableFuture[]::new)
    );
  }

  /**
   * Synchronizes the index according to the given diff. Instead of iterating
   * over an entire array of handles (as does {@link #sync(Handle[])}), only the
   * change to the handles given to this session last will be synced.
   *
   * @param diff Change to the handles provided to the last call to
   *   {@link #sync(Handle[])} to be synced with this session.
   * @param handles Result of having merged the diff with the handles with which
   *   this session was last synced.
   */
  void sync(final InstallationDiff diff, final Handle[] handles) {
    final AppSearchSession delegate = getDelegate();
    final IntFunction<CompletableFuture<Void>> synchronizer =
      switch (diff.kind) {
        case INSTALLATION ->
          index -> putDocumentAsync(delegate, this.handles[index]);
        case REPLACEMENT ->
          index -> updateDocumentAsync(delegate, this.handles[index]);
        case UNINSTALLATION ->
          index -> removeDocumentAsync(delegate, this.handles[index]);
      };
    this.handles = handles;
    awaitAll(
      stream(diff.indices)
        .parallel()
        .mapToObj(synchronizer)
        .toArray(CompletableFuture[]::new)
    );
  }

  void end() {
    final AppSearchSession delegate = getDelegate();
    clear(delegate);
    delegate.close();
  }

  private AppSearchSession getDelegate() {
    final boolean wasDone = delegateFuture.isDone();
    final AppSearchSession delegate = await(delegateFuture);
    if (!wasDone)
      await(delegate.setSchemaAsync(SET_SCHEMA_REQUEST));
    return delegate;
  }

  private void clear(final AppSearchSession delegate) {
    if (handles == null)
      return;
    awaitAll(
      stream(this.handles)
        .map(handle -> removeDocumentAsync(delegate, handle))
        .toArray(CompletableFuture[]::new)
    );
  }

  private static <Value> Value await(final Future<Value> future) {
    try {
      return future.get();
    } catch (final CancellationException
                 | ExecutionException
                 | InterruptedException cause) {
      throw new RuntimeException(cause);
    }
  }

  @SuppressWarnings("rawtypes")
  private static void awaitAll(final CompletableFuture[] futures) {
    CompletableFuture.allOf(futures).join();
  }

  private static CompletableFuture<Void> updateDocumentAsync(
    final AppSearchSession delegate,
    final Handle handle
  ) {
    return removeDocumentAsync(delegate, handle)
      .thenCompose(theVoid -> putDocumentAsync(delegate, handle));
  }

  private static CompletableFuture<Void> putDocumentAsync(
    final AppSearchSession delegate,
    final Handle handle
  ) {
    try {
      final var document = new HandleDocument(handle);
      final PutDocumentsRequest request =
        new PutDocumentsRequest.Builder()
          .addDocuments(document)
          .build();
      return toCompletableFuture(delegate.putAsync(request)).thenRun(NO_OP);
    } catch (final AppSearchException cause) {
      throw new RuntimeException(cause);
    }
  }

  private static CompletableFuture<Void> removeDocumentAsync(
    final AppSearchSession delegate,
    final Handle handle
  ) {
    final RemoveByDocumentIdRequest request =
      new RemoveByDocumentIdRequest.Builder(HandleDocument.NAMESPACE)
        .addIds(handle.id)
        .build();
    return toCompletableFuture(delegate.removeAsync(request)).thenRun(NO_OP);
  }
}