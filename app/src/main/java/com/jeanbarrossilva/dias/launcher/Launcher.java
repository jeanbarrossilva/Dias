package com.jeanbarrossilva.dias.launcher;

import android.content.ActivityNotFoundException;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;
import androidx.collection.MutableObjectIntMap;

import com.jeanbarrossilva.OneTimeCopyOnWriteArrayList;
import com.jeanbarrossilva.dias.core.IntHashSets;
import com.jeanbarrossilva.dias.launcher.installation.InstallationChangeReceiver;
import com.jeanbarrossilva.dias.launcher.installation.InstallationDiff;
import com.jeanbarrossilva.dias.launcher.persistence.PinDatabase;

import org.agrona.collections.IntHashSet;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import static androidx.room.Room.inMemoryDatabaseBuilder;
import static java.util.Arrays.binarySearch;
import static java.util.Arrays.stream;
import static java.util.Objects.hash;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

/**
 * View of both the homescreen and the drawer. Although they're shown separately
 * to the user, this is the main model of Dias and joins both concepts, allowing
 * for every supported operation to be performed.
 */
public final class Launcher implements Closeable {
  private final WeakReference<Context> contextRef;
  private final InstallationChangeReceiver installationChangeReceiver;
  private final PinDatabase pinDatabase;
  private Handle[] handles;
  private final MutableObjectIntMap<String> indexing;
  private Consumer<Pinning> onPinningListener;

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

  /**
   * Relation between indices of handles and whether those handles are pinned.
   * Particularly useful when listening to calls to {@link #pin(String)} and
   * {@link #unpin(String)}.
   */
  public static final class Pinning {
    /** Indices of handle that have been pinned or unpinned. */
    @NonNull final IntHashSet indices;

    /** Whether the handles at the given indices is pinned. */
    final boolean arePinned;

    public Pinning(@NonNull final IntHashSet indices, final boolean arePinned) {
      this.indices = indices;
      this.arePinned = arePinned;
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
      return obj instanceof Pinning typedObj
        && indices.equals(typedObj.indices)
        && arePinned == typedObj.arePinned;
    }

    @Override
    public int hashCode() {
      return hash(indices, arePinned);
    }

    @Override
    public String toString() {
      return "Pinning(indices=" + indices + ", arePinned=" + arePinned + ')';
    }
  }

  /**
   * Instantiates a launcher containing handles to every user-visible installed
   * application on the device.
   *
   * @param context Context through which information about installed
   *   applications are queried and parsed into handles.
   * @param pinDatabase Database for persisting pinned handles.
   * @throws NullPointerException If the database is {@code null}.
   */
  public Launcher(final Context context, final PinDatabase pinDatabase)
    throws NullPointerException {
    this(
      context,
      pinDatabase,
      context == null || context.getPackageManager() == null
        ? new Handle[0]
        : Handle.queryAll(context.getPackageManager(), Handle.QUERY_INTENT)
    );
  }

  /**
   * Instantiates a launcher containing the given handles, all unpinned.
   *
   * @param context Context to register the installation change receiver to.
   * @param pinDatabase Database for persisting pinned handles.
   * @param handles Unique handles to all applications visible to the user,
   *  sorted by label.
   * @throws NullPointerException If the database is {@code null}.
   */
  @VisibleForTesting
  public Launcher(
    final Context context,
    final PinDatabase pinDatabase,
    final Handle[] handles
  )
    throws NullPointerException {
    this.contextRef = new WeakReference<>(context);
    this.pinDatabase = requireNonNull(pinDatabase, "pinDatabase");
    this.handles = handles;
    this.indexing = new MutableObjectIntMap<>(handles.length);
    for (int index = 0; index < handles.length; index++)
      this.indexing.put(handles[index].id, index);
    this.onPinningListener = null;

    this.installationChangeReceiver =
      new InstallationChangeReceiver(handles, diff -> {
        this.handles = diff.merge(handles);
        if (diff.kind != InstallationDiff.Kind.UNINSTALLATION)
          return;
        for (final int index: diff.indices)
          this.indexing.removeValueAt(index);
        unpin(
          /* ids = */     intersection(findPins(), diff.handles),
          /* indices = */ IntHashSets.of(diff.indices)
        );
      });
    if (context != null)
      context.registerReceiver(installationChangeReceiver,
        InstallationChangeReceiver.FILTER);
  }

  /**
   * Obtains the unique handles to all applications visible to the user, sorted
   * by label.
   */
  @NonNull
  public List<Handle> getHandles() {
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
  public boolean hasHandles() {
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
  public boolean hasPins() {
    final int pinCount = pinDatabase.getDao().count();
    return pinCount > 0 && pinCount < handles.length;
  }

  /**
   * Computes which handles are pinned, returning them sorted by label.
   *
   * @see #pin(String)
   */
  @NonNull
  public Handle[] findPins() {
    return stream(pinDatabase.getDao().selectAll())
      .map(pin -> pin.toHandle(this))
      .toArray(Handle[]::new);
  }

  /**
   * Returns the label of the handle identified by the given ID.
   *
   * @param id ID of the handle whose label will be returned.
   * @throws HandleNotFoundException If the handle doesn't belong to this
   *   launcher.
   */
  @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
  public String label(final String id) throws HandleNotFoundException {
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
  public void pin(final String id) throws HandleNotFoundException {
    assertContains(id);
    final boolean didPin = pinDatabase.getDao().insert(id);
    if (onPinningListener == null || !didPin)
      return;
    final var pinning = new Pinning(IntHashSets.of(getIndex(id)), true);
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
  public void pin(Set<String> ids) throws HandleNotFoundException {
    assertContainsAll(ids);
    final boolean didPin =
      pinDatabase.getDao().insert(ids.toArray(new String[0])) > 0;
    if (onPinningListener == null || !didPin)
      return;
    final var pinning = new Pinning(getIndices(ids), true);
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
  public void unpin(final String id) throws HandleNotFoundException {
    assertContains(id);
    final boolean didUnpin = pinDatabase.getDao().delete(id);
    if (onPinningListener == null || !didUnpin)
      return;
    final int index = getIndex(id);
    final var pinning = new Pinning(IntHashSets.of(index), false);
    onPinningListener.accept(pinning);
  }

  /**
   * Determines whether the handle identified by the given ID is pinned.
   *
   * @param id ID of the handle that may be pinned.
   * @return {@code true} if the handle belongs to this launcher and is pinned;
   *   otherwise, {@code false}.
   */
  public boolean isPinned(final String id) {
    return pinDatabase.getDao().isPinned(id);
  }

  /**
   * Unpins the pinned handles at each given indices.
   *
   * @param ids IDs of the handles to unpin.
   * @see #pin(String)
   * @throws HandleNotFoundException If a handle doesn't belong to this
   *   launcher.
   */
  public void unpin(final Set<String> ids) throws HandleNotFoundException {
    unpin(ids, getIndices(ids));
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
  public void setOnPinningListener(
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
   * Clears all tables of the underlying database.
   * <p>
   * <b>WARNING</b>: All pins will be lost. This is a destructive operation and
   * cannot be undone.
   *
   */
  public void clear() {
    pinDatabase.clearAllTables();
  }

  /**
   * Removes the on-pinning listener and closes the connection to the underlying
   * database.
   */
  @Override
  public void close() {
    final Context context = contextRef.get();
    pinDatabase.close();
    if (context != null)
      context.unregisterReceiver(installationChangeReceiver);
    onPinningListener = null;
  }

  /**
   * Instantiates a launcher for demonstration purposes.
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
  public static Launcher sample(final Context context)
    throws NullPointerException {
    return new Launcher(
                          context,
      /* pinDatabase = */ inMemoryDatabaseBuilder(context, PinDatabase.class)
                            .allowMainThreadQueries()
                            .build(),
                          Handle.SAMPLES
    );
  }

  private static Set<String> intersection(final Handle[] lhs,
                                          final Handle[] rhs) {
    if (lhs == null || rhs == null)
      return null;
    if (Arrays.equals(lhs, rhs))
      return stream(lhs).map(handle -> handle.id).collect(toSet());
    final var list = new ArrayList<String>(
      /* initialCapacity = */ (lhs.length + rhs.length) / 2
    );
    for (final Handle a: lhs)
      if (binarySearch(rhs, a) >= 0)
        list.add(a.id);
    return new HashSet<>(list);
  }

  /**
   * Returns the handle identified by the given ID.
   *
   * @param id ID of the handle to be returned.
   * @see Handle#id
   * @throws HandleNotFoundException If the handle doesn't belong to this
   *   launcher.
   */
  @VisibleForTesting
  Handle get(final String id) throws HandleNotFoundException {
    return handles[indexing.get(id)];
  }

  private void unpin(final Set<String> ids, final IntHashSet indices) {
    assertContainsAll(ids);
    final String[] idsAsArray = ids.toArray(new String[0]);
    final boolean didUnpin = pinDatabase.getDao().delete(idsAsArray) > 0;
    if (onPinningListener == null || !didUnpin)
      return;
    final var pinning = new Pinning(indices, false);
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