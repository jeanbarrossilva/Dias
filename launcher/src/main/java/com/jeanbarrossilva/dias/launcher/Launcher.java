/*
 * Copyright © 2026 Jean Silva
 *
 * Dias is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 2 of the License, or (at your option) any later
 * version.
 *
 * Dias is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Dias. If not, see https://www.gnu.org/licenses.
 */

package com.jeanbarrossilva.dias.launcher;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;
import androidx.collection.MutableObjectIntMap;

import com.jeanbarrossilva.OneTimeCopyOnWriteArrayList;
import com.jeanbarrossilva.dias.launcher.local.LocalLauncher;
import com.jeanbarrossilva.dias.launcher.system.SystemLauncher;
import com.jeanbarrossilva.dias.launcher.util.IntHashSets;
import com.jeanbarrossilva.dias.launcher.installation.InstallationChangeReceiver;
import com.jeanbarrossilva.dias.launcher.system.persistence.PinDatabase;

import org.agrona.collections.IntHashSet;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import static com.jeanbarrossilva.dias.launcher.util.Arrays.intersection;

/**
 * View of both the homescreen and the drawer. Although they're shown separately
 * to the user, this is the main model of Dias and joins both concepts, allowing
 * for every supported operation to be performed.
 */
public abstract class Launcher implements Closeable {
  private final WeakReference<Context> contextRef;
  private final InstallationChangeReceiver installationChangeReceiver;
  private Handle[] handles;
  private final MutableObjectIntMap<String> indexing;
  private final HashMap<UUID, SearchSession> searching;
  private Consumer<Pinning> onPinningListener;

  /// Defines which handles will be in a launcher being instantiated: ones to
  /// the actual apps installed on the device; or ones to sample, potentially
  /// nonexistent apps, for demonstration purposes.
  ///
  /// @see Fetch
  /// @see Sample
  public static sealed abstract class SetupStrategy
    permits SetupStrategy.Fetch, SetupStrategy.Sample {
    private static final class Fetch extends SetupStrategy {
      public static final Fetch INSTANCE = new Fetch();

      private static final Intent HANDLE_QUERY_INTENT =
        Handle.createQueryIntent();

      @NonNull
      @Override
      Handle[] createHandles(@Nullable final Context context) {
        return context == null || context.getPackageManager() == null
          ? new Handle[0]
          : Handle.queryAll(context.getPackageManager(), HANDLE_QUERY_INTENT)
            .values()
            .toArray(Handle[]::new);
      }
    }

    private static final class Sample extends SetupStrategy {
      private static final Sample INSTANCE = new Sample();

      private Sample() {}

      @NonNull
      @Override
      Handle[] createHandles(@Nullable final Context context) {
        return Handle.createSamples();
      }
    }

    @NonNull
    abstract Handle[] createHandles(@Nullable final Context context);

    /// Strategy for configuring a launcher with handles to apps installed on
    /// the device.
    @NonNull
    public static SetupStrategy fetch() {
      return Fetch.INSTANCE;
    }

    /// Strategy for configuring a launcher with sample handles, to apps that
    /// might not be installed on the device, for mere demonstration purposes
    /// (e.g., UI previews and tests).
    @NonNull
    public static SetupStrategy sample() {
      return Sample.INSTANCE;
    }
  }

  /**
   * Exception thrown whenever handles that don't belong to a launcher are
   * requested to be pinned or unpinned in that same launcher.
   *
   * @see #pin(Set)
   * @see #unpin(Set)
   */
  public static final class HandleNotFoundException
    extends NoSuchElementException {
    /**
     * IDs of the handles that weren't found.
     *
     * @see Handle#id
     */
    final Set<String> ids;

    HandleNotFoundException(final String id) {
      super("no handle identified as " + id + " found");
      this.ids = Set.of(id);
    }

    HandleNotFoundException(final Set<String> ids) {
      super("one or more handles weren't found: " + ids);
      this.ids = ids;
    }
  }

    /// Relation between indices of handles and whether those handles are pinned.
    /// Particularly useful when listening to calls to {@link #pin(String)} and
    /// {@link #unpin(String)}.
    ///
    /// @param indices  Indices of handle that have been pinned or unpinned.
    /// @param arePinned  Whether the handles at the given indices is pinned.
    public record Pinning(
      @NonNull
      @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
      IntHashSet indices,

      @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
      boolean arePinned
    ) {
    @Override
      public String toString() {
        return "Pinning[indices=" + indices + ", arePinned=" + arePinned + ']';
      }
    }

  /**
   * Instantiates a launcher.
   *
   * @param context Context through which information about installed
   *   applications are queried and parsed into handles.
   * @param handles Unique handles to all applications visible to the user,
   *   sorted by label.
   * @see SearchSession
   * @throws NullPointerException If the pin database, the search index name,
   *   the name of the search index or the handle array is {@code null}.
   */
  @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
  @SuppressWarnings("KotlinInternalInJava")
  protected Launcher(
    @Nullable final Context context,
    @NonNull final Handle[] handles
  ) throws NullPointerException {
    this.contextRef = new WeakReference<>(context);
    this.handles = handles;
    this.indexing = new MutableObjectIntMap<>(handles.length);
    for (int index = 0; index < handles.length; index++)
      this.indexing.put(handles[index].id, index);
    this.onPinningListener = null;
    this.searching = new HashMap<>();

    this.installationChangeReceiver =
      new InstallationChangeReceiver(this.handles, diff -> {
        this.handles = diff.merge(this.handles);
        switch (diff.kind) {
          case INSTALLATION, REPLACEMENT -> {
            for (int index = 0; index < diff.handleIDs.length; index++)
              this.indexing.put(diff.handleIDs[index], diff.indices[index]);
          }
          case UNINSTALLATION -> {
            for (final int index: diff.indices)
              this.indexing.removeValueAt(index);
            unpin(
              Set.of(intersection(getPinIDs(), diff.handleIDs, String[].class)),
              IntHashSets.of(diff.indices)
            );
          }
        }
        for (final SearchSession searchSession: this.searching.values())
          searchSession.sync(context, diff, this.handles);
      });
    if (context != null)
      context.registerReceiver(
        installationChangeReceiver,
        InstallationChangeReceiver.createIntentFilter(),
        Context.RECEIVER_EXPORTED
      );
  }

  /**
   * Obtains the unique handles to all applications visible to the user, sorted
   * by label.
   */
  @NonNull
  public final List<Handle> getHandles() {
    return new OneTimeCopyOnWriteArrayList<>(
                                     handles,
      /* isImmutableTreeSetLike = */ true
    );
  }

  /**
   * Returns whether there are handles in this launcher.
   * <p>
   * This method returning {@code true} guarantees that a call to {@link
   * #getHandles()} results in a list containing at least one handle; otherwise,
   * that list will be empty.
   */
  public final boolean hasHandles() {
    return handles.length > 0;
  }

  /**
   * Returns whether there's any <i>meaningful</i> pin in this launcher: this
   * means that there must be at least one pinned handle; and not all handles
   * are pinned.
   * <p>
   * This method returning {@code true} guarantees that a call to  {@link
   * #findPins()} results in an array containing at least one pin, and that the
   * array's contents differ from those of the list returned by {@link
   * #getHandles()}.
   */
  public final boolean hasPins() {
    final int pinCount = countPins();
    return pinCount > 0 && pinCount < handles.length;
  }

  /**
   * Computes which handles are pinned, returning them sorted by label.
   *
   * @see #pin(String)
   */
  @NonNull
  public abstract Handle[] findPins();

  /**
   * Returns the label of the handle identified by the given ID.
   *
   * @param id ID of the handle whose label will be returned.
   * @throws HandleNotFoundException If the handle doesn't belong to this
   *   launcher.
   */
  @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
  public final String label(final String id) throws HandleNotFoundException {
    return get(id).label;
  }

  /**
   * Pins the given unpinned handle.
   *
   * @param id ID of the handle to be pinned.
   * @see Handle#id
   * @throws HandleNotFoundException If the handle doesn't belong to this
   *   launcher.
   */
  public final void pin(final String id) throws HandleNotFoundException {
    assertContains(id);
    final var pinning = new Pinning(IntHashSets.of(getIndex(id)), true);
    final boolean didPin = onPinning(pinning);
    if (!didPin)
      return;
    if (onPinningListener != null)
      onPinningListener.accept(pinning);
  }

  /**
   * Pins the given unpinned handles.
   *
   * @param ids IDs of the handles to pin.
   * @see Handle#id
   * @throws HandleNotFoundException If a handle doesn't belong to this
   *   launcher.
   */
  public final void pin(Set<String> ids) throws HandleNotFoundException {
    assertContainsAll(ids);
    final var pinning = new Pinning(getIndices(ids), true);
    final boolean didPin = onPinning(pinning);
    if (!didPin)
      return;
    if (onPinningListener != null)
      onPinningListener.accept(pinning);
  }

  /**
   * Unpins the given pinned handle.
   *
   * @param id ID of the handle to unpin.
   * @see Handle#id
   * @throws HandleNotFoundException If the handle doesn't belong to this
   *   launcher.
   */
  public final void unpin(final String id) throws HandleNotFoundException {
    assertContains(id);
    final int index = getIndex(id);
    final var pinning = new Pinning(IntHashSets.of(index), false);
    final boolean didUnpin = onPinning(pinning);
    if (!didUnpin)
      return;
    if (onPinningListener != null)
      onPinningListener.accept(pinning);
  }

  /**
   * Determines whether the handle identified by the given ID is pinned.
   *
   * @param id ID of the handle that may be pinned.
   * @return {@code true} if the handle belongs to this launcher and is pinned;
   *   otherwise, {@code false}.
   */
  public abstract boolean isPinned(@Nullable final String id);

  /**
   * Unpins the pinned handles at each given indices.
   *
   * @param ids IDs of the handles to unpin.
   * @see #pin(String)
   * @throws HandleNotFoundException If a handle doesn't belong to this
   *   launcher.
   */
  public final void unpin(final Set<String> ids) throws HandleNotFoundException {
    unpin(ids, getIndices(ids));
  }

  /**
   * Initiates a session that allows for searching for handles throughout this
   * entire launcher. Many sessions can be started, with each being independent
   * of the other.
   * <p>
   * To free resources, end the session after its use.
   *
   * @return The started session, or {@code null} if a context is required but
   *   the given one has been garbage-collected or is null.
   * @see #endSearchSession(SearchSession)
   */
  @Nullable
  public final SearchSession startSearchSession() throws NullPointerException {
    try {
      final Context context = getContext();
      final var result = createSearchSession(context);
      result.sync(context, handles);
      searching.put(result.id, result);
      return result;
    } catch (final SearchSession.UncontextualizedException exception) {
      return null;
    }
  }

  /**
   * Terminates a search session that's been started and frees its resources.
   *
   * @param searchSession Session to end.
   * @see #startSearchSession()
   */
  public final void endSearchSession(
    @Nullable final SearchSession searchSession
  ) {
    if (searchSession == null)
      return;
    searchSession.end(getContext());
    searching.remove(searchSession.id);
  }

  /**
   * Listens to each effectful call to {@link #pin(String)} and
   * {@link #unpin(String)}, notifying the given listener about the handles that
   * were newly pinned/unpinned.
   * <p>
   * The listener will be notified immediately after a call to this method in
   * case it's different from the previous listener and this launcher has pins
   * (according to the semantics of {@link #hasPins()}).
   *
   * @param listener Listener to be notified of changes regarding pinning
   *   handles.
   */
  public final void setOnPinningListener(
    @NonNull final Consumer<@NotNull Pinning> listener
  ) {
    if (Objects.equals(onPinningListener, listener))
      return;
    onPinningListener = listener;
    if (!hasPins())
      return;
    final var pins = findPins();
    final var pinIDs = new HashSet<String>(pins.length);
    for (final Handle pin: pins)
      pinIDs.add(pin.id);
    final IntHashSet pinIndices = getIndices(pinIDs);
    final var pinning = new Pinning(pinIndices, true);
    listener.accept(pinning);
  }

  /**
   * Unpins every handle that's been pinned in this launcher.
   * <p>
   * <b>WARNING</b>: All pins will be lost. This is a destructive operation and
   * cannot be undone.
   *
   */
  public abstract void clear();

  /**
   * Removes the on-pinning listener and ends all search sessions started in
   * this launcher.
   *
   * @see #setOnPinningListener(Consumer)
   * @see #startSearchSession()
   */
  @Override
  public final void close() {
    final Context context = getContext();
    onClose();
    if (context != null)
      context.unregisterReceiver(installationChangeReceiver);
    onPinningListener = null;
    for (final SearchSession searchSession: searching.values())
      endSearchSession(searchSession);
  }

  /// Returns the context being referenced by this launcher.
  @Nullable
  public final Context getContext() {
    return contextRef.get();
  }

   /// Instantiates a launcher that depends on features of an Android device and
   /// contains handles to every user-visible installed application.
   ///
   /// @param context Context through which information about installed
   ///   applications are queried and parsed into handles.
   /// @param pinDatabase Database for persisting pinned handles.
   /// @param searchIndexName Name of the index in which info about each handle,
   ///   tailored for a session, is persisted.
   /// @param setupStrategy How the launcher should be configured: should its
   ///   handles be ones to the apps installed on the device
   ///   ([fetched][SetupStrategy#fetch()]), or sample handles?
   /// @see SearchSession
   /// @throws NullPointerException If the pin database, the search index name
   ///   or the name of the search index is {@code null}.
  @NonNull
   public static Launcher system(
    @Nullable final Context context,
    @NonNull final PinDatabase pinDatabase,
    @NonNull final String searchIndexName,
    @Nullable final SetupStrategy setupStrategy
  ) {
    return new SystemLauncher(
      context,
      pinDatabase,
      searchIndexName,
      setupStrategy == null
        ? new Handle[0]
        : setupStrategy.createHandles(context)
    );
  }

  /**
   * Instantiates a launcher for demonstration purposes, with sample handles and
   * in-memory storage.
   * <p>
   * <b>NOTE</b>: Trying to launch one of the returned launcher's handles will
   * likely throw an {@link ActivityNotFoundException}: they aren't intended to
   * be handles to real applications.
   *
   * @param context Context for the underlying in-memory database.
   * @see Handle#launch(Context)
   * @throws NullPointerException If the context is {@code null}.
   */
  @NonNull
  public static Launcher local(@NonNull final Context context)
    throws NullPointerException {
    return new LocalLauncher(context);
  }

  /**
   * Returns the handle identified by the given ID.
   *
   * @param id ID of the handle to be returned.
   * @see Handle#id
   * @throws HandleNotFoundException If the handle doesn't belong to this
   *   launcher.
   */
  @NonNull
  @VisibleForTesting
  final Handle get(@Nullable final String id) throws HandleNotFoundException {
    try {
      return handles[indexing.get(id)];
    } catch (final NoSuchElementException exception) {
      throw new HandleNotFoundException(id);
    }
  }

  /// Returns the internal array of handles.
  protected final Handle[] getBackingHandles() {
    return handles;
  }

  /// Tries to find the pins at each given index, with out-of-bounds indices or
  /// ones of unpinned handles being mapped to `null` handles in the returned
  /// array.
  ///
  /// @return The pins at the indices, in the same order as the indices
  ///   themselves; or `null` if the index set is `null`.
  @Nullable
  protected final Handle[] getPins(@Nullable final IntHashSet indices) {
    if (indices == null)
      return null;
    final var result = new Handle[indices.size()];
    if (indices.isEmpty())
      return result;
    final IntHashSet.IntIterator indexIterator = indices.iterator();
    int count = 0;
    for (int index = indexIterator.nextValue();
         indexIterator.hasNext();
         index = indexIterator.nextValue(), count++) {
      final Handle handle = handles[index];
      if (isPinned(handle.id))
        result[count] = handle;
    }
    return result;
  }

  /// Computes the amount of handles that are pinned. This is the length of the
  /// array returned by a call to [#findPins()].
  protected abstract int countPins();

  /// Returns the ID of each pinned handle.
  @NonNull
  protected abstract String[] getPinIDs();

  /// Toggles whether the specified handles are pinned: for a `true`
  /// [Pinning#arePinned()], pins the handles; for a `false` one, unpins them.
  ///
  /// @param pinning Information about the handles whose pin will be toggled.
  /// @return Whether the given handles' were pinned or unpinned, depending on
  ///   which was intended. `true` denotes that the call to this function
  ///   changed the launcher's state, while `false` implies the opposite.
  protected abstract boolean onPinning(@NonNull final Pinning pinning);

  /**
   * Instantiates a search session appropriate for this launcher.
   *
   * @param context Context in which the session will be created.
   */
  protected abstract SearchSession createSearchSession(
    @Nullable final Context context
  );

  /// Callback called once this launcher gets closed.
  ///
  /// @see close()
  protected abstract void onClose();

  private void unpin(
    @NonNull final Set<String> ids,
    @NonNull final IntHashSet indices
  ) throws HandleNotFoundException {
    assertContainsAll(ids);
    final Pinning pinning = new Pinning(indices, false);
    final boolean didUnpin = onPinning(pinning);
    if (!didUnpin)
      return;
    if (onPinningListener != null)
      onPinningListener.accept(pinning);
  }

  private void assertContains(final String id) throws HandleNotFoundException {
    if (!indexing.contains(id))
      throw new HandleNotFoundException(id);
  }

  private void assertContainsAll(final Set<String> ids)
    throws HandleNotFoundException {
    if (size() < ids.size())
      throw new HandleNotFoundException(ids);
    for (final String id: ids)
      if (!indexing.contains(id))
        throw new HandleNotFoundException(id);
  }

  private IntHashSet getIndices(final Set<String> ids)
    throws HandleNotFoundException {
    final IntHashSet indices = new IntHashSet(ids.size());
    for (final String id: ids)
      indices.add(getIndex(id));
    return indices;
  }

  private int getIndex(final String id) throws HandleNotFoundException {
    try {
      return indexing.get(id);
    } catch (final NoSuchElementException exception) {
      throw new HandleNotFoundException(id);
    }
  }

  private int size() {
    return handles.length;
  }
}