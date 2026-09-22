package com.jeanbarrossilva.dias;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import org.agrona.collections.IntHashSet;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import static com.jeanbarrossilva.dias.Iterables.joinToString;
import static java.util.Arrays.binarySearch;
import static java.util.Objects.hash;

/**
 * View of both the homescreen and the drawer. Although they're shown separately
 * to the user, this is the main model of Dias and joins both concepts, allowing
 * for every supported operation to be performed.
 */
public final class Launcher implements Closeable {
  /**
   * Unique handles to all applications visible to the user, sorted by label.
   */
  @NonNull public final Handle[] handles;

  @NonNull private final WeakReference<Context> contextRef;
  @NonNull private final IntHashSet pinIndices;
  @Nullable private Consumer<@NotNull Pinning> onPinningListener;

  private static final Intent queryIntent =
    new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);

  /** Holds information about a user-visible, installed application. */
  public static final class Handle implements Comparable<Handle> {
    /** ID of the activity associated to this handle. */
    @NonNull public final ComponentName name;

    /** Localized text considered to be the name of the application. */
    @NonNull public final String label;

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    public Handle(
      @NonNull final String packageName,
      @NonNull final String activityName,
      @NonNull final String label
    ) {
      this.name = new ComponentName(packageName, activityName);
      this.label = label;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
      return obj instanceof Handle typedObj
        && name.equals(typedObj.name)
        && label.equals(typedObj.label);
    }

    @Override
    public int hashCode() {
      return hash(name, label);
    }

    @Override
    public int compareTo(Handle o) {
      return label.compareTo(o.label);
    }

    @Nullable
    static Handle parse(
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
   * Relation between indices of handles and whether those handles are pinned.
   * Particularly useful when listening to calls to {@link #pin(int)} and {@link
   * #unpin(int)}.
   */
  public static final class Pinning {
    /** Indices of handle that have been pinned or unpinned. */
    @NonNull final IntHashSet indices;

    /** Whether the handles at the given indices is pinned. */
    final boolean arePinned;

    Pinning(@NonNull final IntHashSet indices, final boolean arePinned) {
      this.indices = indices;
      this.arePinned = arePinned;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
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
   * @param context Context through which the main activity of each application
   *   associated to the found handles may be started.
   *   <p>
   *   In this constructor, it's also used to query which applications are
   *   installed and parse information on them to handles.
   */
  public Launcher(@NonNull final Context context) {
    this(
      context,
      context.getPackageManager() instanceof PackageManager packageManager
        ? queryHandles(packageManager)
        : new Handle[0]
    );
  }

  /**
   * Instantiates a launcher containing the given handles, all unpinned.
   *
   * @param context Context through which the main activity of each application
   *   associated to the found handles may be started.
   * @param handles Unique handles to all applications visible to the user,
   *  sorted by label.
   */
  @VisibleForTesting
  public Launcher(
    @NonNull final Context context,
    @NonNull final Handle[] handles
  ) {
    this.contextRef = new WeakReference<>(context);
    this.handles = handles;
    this.pinIndices = new IntHashSet();
    this.onPinningListener = null;
  }

  /**
   * Computes which handles are pinned, returning them sorted by label.
   *
   * @see #pin(int)
   */
  @NonNull
  public Handle[] findPins() {
    final Handle[] pins = new Handle[pinIndices.size()];
    int count = 0;
    for (final int unsortedIndex: pinIndices) {
      final Handle pin = handles[unsortedIndex];
      final int sortedIndex = -binarySearch(pins, 0, count, pin) - 1;
      assert sortedIndex >= 0
        : "handles are required to be given sorted to the launcher; " +
          "violating this invariant results in findPins() returning an " +
          "unordered array of pins, which goes against the method's contract";
      System.arraycopy(
        /* src = */    pins,
        /* srcPos = */ sortedIndex,
        /* dst = */    pins,
        /* dstPos = */ sortedIndex + 1,
        /* length = */ count - sortedIndex
      );
      pins[sortedIndex] = pin;
      count++;
    }
    return pins;
  }

  /**
   * Determines whether the handle at the given indices is pinned.
   *
   * @param index Index of the handle in this launcher.
   * @throws IndexOutOfBoundsException If the index is negative or greater than
   *   that of the last handle in this launcher.
   */
  public boolean isPinned(final int index)
    throws IndexOutOfBoundsException {
    if (!isBoundedIndex(index))
      throw new IndexOutOfBoundsException(index);
    return pinIndices.contains(index);
  }

  /**
   * Pins the unpinned handle at the given indices.
   *
   * @param index Index of the handle to pin.
   * @see #findPins()
   * @see #unpin(int)
   * @throws IndexOutOfBoundsException If the index is negative or greater than
   *   that of the last handle in this launcher.
   */
  public void pin(final int index) throws IndexOutOfBoundsException {
    if (!isBoundedIndex(index))
      throw new IndexOutOfBoundsException(index);
    final boolean didPin = pinIndices.add(index);
    if (!didPin || onPinningListener == null)
      return;
    final var pinning = new Pinning(IntHashSets.of(index), true);
    onPinningListener.accept(pinning);
  }

  /**
   * Pins the unpinned handles at each given indices.
   *
   * @param indices Indices of the handles to pin.
   * @throws IndexOutOfBoundsException If any of the indices are negative or
   *   greater than that of the last handle in this launcher.
   */
  public void pin(@NonNull final IntHashSet indices)
    throws IndexOutOfBoundsException {
    withBoundedIndices(indices, boundedIndices -> {
      final boolean didPin = pinIndices.addAll(boundedIndices);
      if (!didPin || onPinningListener == null)
        return;
      IntHashSet diff = boundedIndices.difference(pinIndices);
      if (diff == null)
        diff = boundedIndices;
      final var pinning = new Pinning(diff, true);
      onPinningListener.accept(pinning);
    });
  }

  /**
   * Unpins the pinned handle at the given indices.
   *
   * @param index Index of the handle to unpin.
   * @throws IndexOutOfBoundsException If the indices is negative or greater than
   *   that of the last handle in this launcher.
   */
  public void unpin(final int index) throws IndexOutOfBoundsException {
    if (!isBoundedIndex(index))
      throw new IndexOutOfBoundsException(index);
    final boolean didUnpin = pinIndices.remove(index);
    if (!didUnpin || onPinningListener == null)
      return;
    final var pinning = new Pinning(IntHashSets.of(index), false);
    onPinningListener.accept(pinning);
  }

  /**
   * Unpins the pinned handles at each given indices.
   *
   * @param indices Indices of the handles to unpin.
   * @see #pin(int)
   * @throws IndexOutOfBoundsException If any of the indices are negative or
   *   greater than that of the last handle in this launcher.
   */
  public void unpin(@NonNull final IntHashSet indices)
    throws IndexOutOfBoundsException {
    withBoundedIndices(indices, boundedIndices -> {
      final boolean didUnpin = pinIndices.removeAll(boundedIndices);
      if (!didUnpin || onPinningListener == null)
        return;
      final IntHashSet diff = boundedIndices.difference(pinIndices);
      final var pinning = new Pinning(diff, false);
      onPinningListener.accept(pinning);
    });
  }

  /**
   * Listens to each effectful call to {@link #pin(int)} and
   * {@link #unpin(int)}, notifying the given listener about the handles that
   * were newly pinned/unpinned.
   *
   * @param listener Listener to be notified of changes regarding pinning
   *   handles.
   */
  public void setOnPinningListener(
    @NonNull final Consumer<@NotNull Pinning> listener
  ) {
    onPinningListener = listener;
  }

  /**
   * Starts the activity associated to the handle at the given index.
   *
   * @param index Index of the handle to be launched.
   * @see #launch(Handle)
   * @throws NullPointerException If this launcher has been closed, the context
   *   has been garbage-collected, or is null.
   * @throws IndexOutOfBoundsException If the index is negative or greater than
   *   that of the last handle in this launcher.
   */
  public void launch(final int index)
    throws NullPointerException, IndexOutOfBoundsException {
    if (!isBoundedIndex(index))
      throw new IndexOutOfBoundsException(index);
    launch(handles[index]);
  }

  /**
   * Starts the activity associated to the given handle.
   *
   * @param handle Handle to be launched.
   * @see #close()
   * @throws NullPointerException If this launcher has been closed, the context
   *   has been garbage-collected, or is null.
   */
  public void launch(@NonNull final Handle handle) throws NullPointerException {
    final Context context = contextRef.get();
    if (context == null)
      throw new NullPointerException("context");
    final Intent intent = new Intent()
      .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      .setComponent(handle.name);

    // despite the fact that this call may throw, launchers instantiated by the
    // public constructor will have their handles all "pointing" to valid,
    // existent activities, since they were queried from the OS itself.
    //
    // the only scenario in which this *will* throw is when this instance is a
    // result of calling `Launcher(Context, Handle[])` with arbitrary handles,
    // e.g., for testing purposes.
    context.startActivity(intent);
  }

  @Override
  public void close() {
    contextRef.clear();
    onPinningListener = null;
  }

  private boolean isBoundedIndex(final int index) {
    return index >= 0 && index < handles.length;
  }

  private void withBoundedIndices(
    @NonNull final Set<Integer> indices,
    @NonNull final Consumer<IntHashSet> callback
  ) throws IndexOutOfBoundsException {
    if (indices.isEmpty())
      return;
    final var boundedIndices =
      new IntHashSet(/* proposedCapacity = */ indices.size());
    ArrayList<String> outOfBoundsIndices = null;
    for (final int index: indices) {
      if (isBoundedIndex(index))
        boundedIndices.add(index);
      else {
        if (outOfBoundsIndices == null)
          outOfBoundsIndices = new ArrayList<>();
        outOfBoundsIndices.add(String.valueOf(index));
      }
    }
    if (!boundedIndices.isEmpty())
      callback.accept(boundedIndices);
    if (outOfBoundsIndices != null)
      throw new IndexOutOfBoundsException(
        joinToString(outOfBoundsIndices, ", ", " and ")
      );
  }

  @NonNull
  private static Handle[] queryHandles(
    @NonNull final PackageManager packageManager
  ) {
    return packageManager
      .queryIntentActivities(queryIntent, PackageManager.MATCH_DEFAULT_ONLY)
      .stream()
      .filter(Objects::nonNull)
      .map(
        resolveInfo -> Handle.parse(packageManager, resolveInfo.activityInfo)
      )
      .filter(Objects::nonNull)
      .sorted()
      .toArray(Handle[]::new);
  }
}