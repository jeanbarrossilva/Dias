package com.jeanbarrossilva.dias;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.jeanbarrossilva.dias.Objects.as;
import static java.lang.CharSequence.compare;
import static java.util.Objects.hash;

/**
 * A handle contains information about a main, launcher activity that's visible
 * to the user as an application on either the home screen or the drawer. Its
 * API emulates the capabilities of said application's visualization exposed to
 * the user: whether it's pinned, its label and the ability to launch it.
 * <p>
 * The safest way to obtain instances of this type is via
 * {@link Contexts#queryHandles(Context)}. The handles returned by that function
 * are guaranteed to be valid, i.e., point to existing resources accessible to
 * Dias and able to start their respective activity without throwing due to some
 * fault of our own or the caller's.
 *
 * @see #isPinned()
 * @see #label
 */
public final class Handle implements Comparable<Handle> {
  /**
   * Identifier of the application. If this handle resulted from
   * {@link Contexts#queryHandles(Context)}, this ID is guaranteed to be unique
   * among the IDs of those other handles.
   */
  public final int id;

  /** Localized text considered to be the name of the application. */
  @NonNull public final CharSequence label;

  /** {@link #pinIndex} of a handle when it's unpinned. */
  public static final int UNPINNED = -1;

  private int pinIndex;

  Handle(
    final int id,
    @NonNull final CharSequence label
 // @NonNull final Class<? extends Activity> activityClass
  ) {
    this.id = id;
    this.label = label;
    this.pinIndex = UNPINNED;
 // this.activityClass = activityClass;
  }

  @Override
  @SuppressWarnings("EqualsDoesntCheckParameterClass")
  public boolean equals(@Nullable final Object obj) {
    final Handle handleObj = as(Handle.class, obj);
    if (handleObj == null)
      return false;
    return id == handleObj.id
      && label.equals(handleObj.label)
      && pinIndex == handleObj.pinIndex;
   // && activityClass.equals(handleObj.activityClass);
  }

  @Override
  public int hashCode() {
    return hash(id, label, pinIndex /* , activityClass */);
  }

  @NonNull
  @Override
  public String toString() {
    return "Handle(id=%d, label=%s, isPinned=%b)"
      .formatted(id, label, pinIndex /* , activityClass */);
  }

  @Override
  public int compareTo(Handle o) {
    return compare(label, o.label);
  }

  /**
   * Returns the index at which this handle should be alongside other pinned
   * handles on the home screen.
   * <p>
   * The index is either positive, denoting that this handle is, in fact,
   * pinned; or negative, in which case {@link #isPinned()} is {@code false}.
   */
  public int getPinIndex() {
    return pinIndex;
  }

  /**
   * Returns whether this handle should be shown on the home screen; if not, it
   * will only be visible on and launchable from the drawer.
   * <p>
   * By default, a handle is unpinned and, thus, this returns {@code false}.
   *
   * @see #pin(int)
   * @see #unpin()
   */
  public boolean isPinned() {
    return pinIndex > UNPINNED;
  }

  /**
   * Marks this handle as pinned.
   *
   * @param index Index at which this handle should be shown on the home screen,
   *              alongside other pinned handles.
   * @see #isPinned()
   * @see #unpin()
   * @throws IndexOutOfBoundsException If the index is negative.
   */
  public void pin(final int index) throws IndexOutOfBoundsException {
    if (index < 0)
      throw new IndexOutOfBoundsException(
        "handle cannot be pinned at a negative index (%d)".formatted(index)
      );
    pinIndex = index;
  }

  /**
   * Marks this handle as being unpinned.
   *
   * @see #isPinned()
   * @see #pin(int)
   */
  public void unpin() {
    pinIndex = UNPINNED;
  }
}