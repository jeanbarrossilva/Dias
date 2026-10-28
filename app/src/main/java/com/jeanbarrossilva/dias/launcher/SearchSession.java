package com.jeanbarrossilva.dias.launcher;

import androidx.annotation.Nullable;

import com.jeanbarrossilva.OneTimeCopyOnWriteArrayList;
import com.jeanbarrossilva.dias.launcher.installation.InstallationDiff;

import java.util.List;
import java.util.UUID;

import static java.util.Arrays.stream;
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

  private Handle[] handles;

  SearchSession() {
    this.id = randomUUID();
    this.handles = null;
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
    return stream(handles)
      .filter(handle -> handle.label.contains(query))
      .toList();
  }

  /**
   * Synchronizes the index with the given handles. Syncing must be done both
   * immediately after this session has been instantiated and whenever some
   * change occurs to the launcher's handles.
   *
   * @param handles All handles in the launcher.
   * @implNote Because we don't know <i>what</i> changed about the handles, we
   *   wipe the entire index and add a document for every handle. This might be
   *   unnecessarily inefficient (e.g., for installation changes, we could rely
   *   on the diff instead).
   * @see InstallationDiff
   */
  void sync(final Handle[] handles) {
    this.handles = handles;
  }

  void end() {
    // no-op for now. the plan is to implement search with an engine (e.g.,
    // Apache Lucene); doing so would likely require us to deconfigure the
    // search index—hence this end() method.
  }
}