package com.jeanbarrossilva.dias;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;

import com.google.common.collect.ContiguousSet;
import com.jeanbarrossilva.OneTimeCopyOnWriteArrayList;
import com.jeanbarrossilva.dias.core.Comparators;
import com.jeanbarrossilva.dias.core.IntHashSets;
import com.jeanbarrossilva.dias.persistence.Pin;
import com.jeanbarrossilva.dias.persistence.PinDatabase;

import org.agrona.collections.IntHashSet;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import static androidx.room.Room.inMemoryDatabaseBuilder;
import static com.jeanbarrossilva.dias.core.Arrays.binarySearch;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Objects.hash;
import static java.util.Objects.requireNonNull;

/**
 * View of both the homescreen and the drawer. Although they're shown separately
 * to the user, this is the main model of Dias and joins both concepts, allowing
 * for every supported operation to be performed.
 */
public final class Launcher implements Closeable {
  private final PinDatabase pinDatabase;
  private final Handle[] handles;
  private Consumer<Pinning> onPinningListener;

  private static final Intent queryIntent =
    new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);

  /** Holds information about a user-visible, installed application. */
  @SuppressWarnings("ClassCanBeRecord")
  public static final class Handle implements Comparable<Handle> {
    /**
     * Identifier of this handle. Consists of this handle's associated
     * activity's flattened name.
     */
    @NonNull public final String id;

    /** Localized text considered to be the name of the application. */
    @NonNull public final String label;

    private static final Launcher.Handle[] SAMPLES =
      ContiguousSet.closedOpen(0, 13)
        .stream()
        .map(
          index -> new Launcher.Handle(
            /* packageName = */  "com.jeanbarrossilva.dias",
            /* activityName = */ "com.jeanbarrossilva.dias.Activity"
                                   + (index + 1),
            /* label = */        "App " + (index + 1)
          )
        )
        .toArray(Launcher.Handle[]::new);

    @VisibleForTesting static final Launcher.Handle SAMPLE = SAMPLES[0];

    @VisibleForTesting
    Handle(
      final String packageName,
      final String activityName,
      final String label
    ) throws NullPointerException {
      this(/* id = */ packageName + '/' + activityName, label);
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public Handle(final String id, final String label) {
      this.id = id;
      this.label = label;
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
      return obj instanceof Handle typedObj
        && id.equals(typedObj.id)
        && label.equals(typedObj.label);
    }

    @Override
    public int hashCode() {
      return hash(id, label);
    }

    @NonNull
    @Override
    public String toString() {
      return label + " (" + id + ')';
    }

    @Override
    public int compareTo(final Handle o) {
      return label.compareTo(o.label);
    }

    /**
     * Starts the activity associated to this handle.
     *
     * @param context Context from which to start the activity.
     * @throws ActivityNotFoundException If this handle is associated to an
     *   activity nonexistent in the given context.
     */
    public void launch(@NonNull final Context context)
      throws ActivityNotFoundException {
      final Intent intent = new Intent()
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        .setComponent(ComponentName.unflattenFromString(id));
      context.startActivity(intent);
    }

    @Nullable
    private static Handle parse(
      @NonNull final PackageManager packageManager,
      @NonNull final ActivityInfo activityInfo
    ) {
      final String packageName = activityInfo.packageName;
      if (packageName == null)
        return null;
      final String activityName = activityInfo.name;
      if (activityName == null)
        return null;
      final String label = activityInfo.loadLabel(packageManager).toString();
      return new Handle(packageName, activityName, label);
    }
  }

  /**
   * Exception thrown whenever a handle that doesn't belong to a launcher is
   * requested to be pinned or unpinned in that same launcher.
   *
   * @see #pin(String)
   * @see #unpin(String)
   */
  public static final class HandleNotFoundException
    extends NoSuchElementException {
    /**
     * ID of the handle that hasn't been found.
     *
     * @see Handle#id
     */
    final String id;

    HandleNotFoundException(final String id) {
      super("no handle identified as " + id + " found");
      this.id = id;
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
  }

  /**
   * Instantiates a launcher containing handles to every user-visible installed
   * application on the device.
   *
   * @param pinDatabase Database for persisting pinned handles.
   * @param context Context through which information about installed
   *   applications are queried and parsed into handles.
   * @throws NullPointerException If the database is {@code null}.
   */
  public Launcher(final Context context, final PinDatabase pinDatabase)
    throws NullPointerException {
    this(
      pinDatabase,
      context == null || context.getPackageManager() == null
        ? new Handle[0]
        : queryHandles(context.getPackageManager())
    );
  }

  /**
   * Instantiates a launcher containing the given handles, all unpinned.
   *
   * @param pinDatabase Database for persisting pinned handles.
   * @param handles Unique handles to all applications visible to the user,
   *  sorted by label.
   * @throws NullPointerException If the database is {@code null}.
   */
  @VisibleForTesting
  public Launcher(final PinDatabase pinDatabase, final Handle[] handles)
    throws NullPointerException {
    this.pinDatabase = requireNonNull(pinDatabase);
    this.handles = handles == null ? new Handle[0] : handles;
    this.onPinningListener = null;
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
    final PinDatabase.Dao dao = pinDatabase.getDao();
    final int pinCount = dao.count();
    if (pinCount == 0 || pinCount == handles.length)
      return false;
    final Pin[] pins = dao.selectAll();
    for (int index = 0; index < pinCount; index++)
      if (!Objects.equals(pins[index].id, handles[index].id))
        return true;
    return false;
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
    final Handle handle = get(id);
    if (handle == null || isPinned(id))
      return;
    final boolean didPin = pinDatabase.getDao().insert(handle.id);
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
   * @throws HandleNotFoundException If the handle doesn't belong to this
   *   launcher.
   */
  public void pin(Set<String> ids) throws HandleNotFoundException {
    final Handle[] handles = getHandles(ids);
    if (handles == null || handles.length == 0)
      return;
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
    final Handle handle = get(id);
    if (handle == null)
      throw new HandleNotFoundException(id);
    if (onPinningListener == null || !isPinned(id))
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
   */
  public void unpin(final Set<String> ids) throws HandleNotFoundException {
    final Handle[] handles = getHandles(ids);
    if (handles == null || handles.length == 0)
      return;
    final String[] idsAsArray = ids.toArray(new String[0]);
    final boolean didUnpin = pinDatabase.getDao().delete(idsAsArray) > 0;
    if (onPinningListener == null || !didUnpin)
      return;
    final var pinning = new Pinning(getIndices(ids), true);
    onPinningListener.accept(pinning);
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
    pinDatabase.close();
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
      /* pinDatabase = */ inMemoryDatabaseBuilder(context, PinDatabase.class)
                            .allowMainThreadQueries()
                            .build(),
                          Handle.SAMPLES
    );
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
    return handles[getIndex(id)];
  }

  private Handle[] getHandles(final Set<String> ids)
    throws HandleNotFoundException {
    final var result = new Handle[ids.size()];
    final Iterator<String> idIterator = ids.iterator();
    for (int index = 0; idIterator.hasNext(); index++) {
      final String id = idIterator.next();
      if (id == null)
        continue;
      result[index] = get(id);
    }
    return stream(result).filter(Objects::nonNull).toArray(Handle[]::new);
  }

  private IntHashSet getIndices(final Set<String> ids)
    throws HandleNotFoundException {
    final IntHashSet indices = new IntHashSet(ids.size());
    for (final String id: ids)
      indices.add(getIndex(id));
    return indices;
  }

  private int getIndex(final String id) throws HandleNotFoundException {
    final int index = binarySearch(
      handles,
      id,
      Comparators.comparingByIndex(asList(handles)),
      handle -> handle.id
    );
    if (index < 0)
      throw new HandleNotFoundException(id);
    return index;
  }

  private static Handle[] queryHandles(final PackageManager packageManager) {
    if (packageManager == null)
      return new Handle[0];
    return packageManager
      .queryIntentActivities(queryIntent, PackageManager.MATCH_ALL)
      .stream()
      .filter(Objects::nonNull)
      .map(r -> Handle.parse(packageManager, r.activityInfo))
      .filter(Objects::nonNull)
      .sorted()
      .toArray(Handle[]::new);
  }
}