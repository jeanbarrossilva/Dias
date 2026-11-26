/*
 * Copyright © 2026 Jean Silva
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see https://www.gnu.org/licenses.
 */

package com.jeanbarrossilva.dias.launcher;

import static com.jeanbarrossilva.dias.launcher.util.UUIDs.toByteArray;
import static java.util.UUID.randomUUID;

import android.content.Context;
import android.icu.text.Normalizer2;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import com.jeanbarrossilva.OneTimeCopyOnWriteArrayList;
import com.jeanbarrossilva.dias.launcher.installation.InstallationDiff;
import com.jeanbarrossilva.dias.launcher.util.UUIDs;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Session for searching for handles with text-based queries.
 *
 * @see #search(Context, String)
 */
public abstract class SearchSession implements Parcelable {
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
  private Consumer<List<Handle>> onSearchListener;

  /// Exception thrown when requesting to search in an ended session.
  ///
  /// @see #search(Context, String)
  public static final class EndedException extends IllegalStateException {
    /// ID of the ended session.
    final UUID id;

    /// Instantiates an [EndedException].
    ///
    /// @param id ID of the ended session.
    EndedException(final UUID id) {
      super("cannot search; session " + id + " has ended");
      this.id = id;
    }
  }

  /// Exception thrown when instantiating a session, or syncing or searching in
  /// a session instantiated from a parcel *without* a context (i.e., with a
  /// `null` context).
  public static final class UncontextualizedException
    extends IllegalStateException {
    /// Instantiates an [UncontextualizedException].
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public UncontextualizedException() {
      super("a context is required, but none was given");
    }
  }

  /// Instantiates a session from a parceled one.
  ///
  /// @param in The parceled session.
  /// @throws NullPointerException If the parceled session is `null`.
  protected SearchSession(@NonNull final Parcel in)
    throws NullPointerException {
    this.id = UUIDs.of(in.createByteArray());
    this.handles =
      in.readParcelableArray(Handle.class.getClassLoader(), Handle.class);
    this.onSearchListener = null;
  }

  /// Instantiates a search session.
  protected SearchSession() {
    this.id = randomUUID();
    this.handles = null;
    this.onSearchListener = null;
  }

  @Override
  public final int describeContents() {
    return 0;
  }

  @CallSuper
  @Override
  public void writeToParcel(@NonNull final Parcel parcel, final int flags) {
    parcel.writeByteArray(toByteArray(id));
    parcel.writeParcelableArray(handles, flags);
  }

  /**
   * Registers a listener to be notified whenever handles are searched for in
   * this session.
   *
   * @param onSearchListener Listener to be notified of searches. If
   *   {@code null}, the previous listener will be removed, and won't be
   *   notified of posterior searches.
   */
  public final void setOnSearchListener(
    @Nullable final Consumer<List<Handle>> onSearchListener
  ) {
    this.onSearchListener = onSearchListener;
  }

  /**
   * Synchronizes the index with the given handles.
   * <p>
   * This method should only be called immediately after this session is
   * instantiated; syncing this way may be expensive, as it wipes out the entire
   * index.
   * <p>
   * For syncing due to changes on handles that have already been synced, and
   * considering only the what's been changed on them, call
   * {@link #sync(Context, InstallationDiff, Handle[])} instead.
   *
   * @param context Context in which the index is or will be created.
   * @param handles All handles in the launcher.
   */
  final void sync(
    @Nullable final Context context,
    @NonNull final Handle[] handles
  ) {
    this.handles = handles;
    onSync(context, handles);
  }

  /**
   * Synchronizes the index according to the given diff. Instead of iterating
   * over an entire array of handles (as does {@link #sync(Context, Handle[])}),
   * only the change to the handles given to this session last will be synced.
   *
   * @param context Context in which the index is or will be created.
   * @param diff Change to the handles provided to the last call to
   *   {@link #sync(Context, Handle[])} to be synced with this session.
   * @param handles Result of having merged the diff with the handles with which
   *   this session was last synced.
   */
  final void sync(
    @Nullable final Context context,
    @NonNull final InstallationDiff diff,
    @NonNull final Handle[] handles
  ) throws UncontextualizedException {
    this.handles = handles;
    if (diff.indices.length == 0)
      return;
    onSync(context, diff);
  }

  /**
   * Finds the handles that match the given query.
   *
   * @param query Text to search handles by. This query will be matched against
   *   the label of every handle, and can be part of that label (e.g., for a
   *   handle labeled "Calculator", {@code query} being {@code "calc"} would
   *   include the handle in the returned array) or match it exactly.
   * @return The search results, or {@code null} if the query is {@code null}.
   * @see Handle#label
   * @throws EndedException If this session has ended.
   * @throws UncontextualizedException If this session is the result of a
   *   deserialization and the given context is {@code null}.
   */
  @Nullable
  public final List<Handle> search(
    @Nullable final Context context,
    @Nullable final String query
  ) throws EndedException, UncontextualizedException {
    if (hasEnded())
      throw new EndedException(id);
    final List<Handle> result;
    if (query == null)
      result = null;
    else if (query.isBlank()) {
      result = new OneTimeCopyOnWriteArrayList<>(
        handles,
        /* isImmutableTreeSetLike = */ true
      );
    } else {
      result = onSearch(context, normalize(query));
    }
    if (onSearchListener != null && result != null)
      onSearchListener.accept(result);
    return result;
  }

  /**
   * Ends this session, permanently releasing its resources.
   *
   * @param context Context in which this session will end.
   */
  final void end(@Nullable final Context context) {
    handles = null;
    if (onSearchListener != null) {
      onSearchListener.accept(List.of());
      setOnSearchListener(null);
    }
    onEnding(context);
  }

  /**
   * Returns the handles from the last sync.
   *
   * @see #sync(Context, Handle[])
   * @see #sync(Context, InstallationDiff, Handle[])
   */
  @Nullable
  protected final Handle[] getHandles() {
    return handles;
  }

  /// Callback called whenever [#sync(Context, Handle\[\])] is called.
  ///
  /// @param context Context in which the index is or will be created.
  /// @param handles All handles in the launcher.
  protected abstract void onSync(
    @Nullable final Context context,
    @NonNull final Handle[] handles
  );

  /// Callback called whenever [#sync(Context, InstallationDiff, Handle\[\])] is
  /// called with a populated diff.
  ///
  /// @param context Context in which the index is or will be created.
  /// @param diff Change to the handles provided to the last call to
  ///   [#sync(Context, Handle\[\])] to be synced with this session.
  protected abstract void onSync(
    @Nullable final Context context,
    @NonNull final InstallationDiff diff
  ) throws UncontextualizedException;

   /// Callback called whenever the user requests to search for handles with a
   /// non-blank query.
   ///
   /// @param context Context in which the search may be performed.
   /// @param query Non-blank, normalized query describing the handles being
   ///   searched for.
   /// @return The handles matching the query.
  @NonNull
  protected abstract List<Handle> onSearch(
    @Nullable final Context context,
    @NonNull final String query
  ) throws UncontextualizedException;

  /// Normalizes a query, regardless of locale.
  ///
  /// @param query Query to normalize.
  /// @return The given query, normalized.
  protected static String normalize(final String query) {
    return Normalizer2.getNFKCInstance()
      .normalize(query)
      .toLowerCase(Locale.ROOT);
  }

  /// Callback called once this session is requested to be ended.
  ///
  /// @param context Context in which this session is ending.
  protected abstract void onEnding(@Nullable final Context context)
    throws UncontextualizedException;

   /// Returns whether this session has ended.
   ///
   /// @see #end(Context)
  protected final boolean hasEnded() {
    return getHandles() == null;
  }
}