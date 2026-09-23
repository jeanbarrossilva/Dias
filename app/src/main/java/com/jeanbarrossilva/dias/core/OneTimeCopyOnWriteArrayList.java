/*
 * Copyright © 2026 Jean Silva
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *                  https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.jeanbarrossilva.dias.core;

import android.annotation.SuppressLint;

import java.lang.reflect.Field;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import static com.jeanbarrossilva.dias.core.ArrayLists.reserveExactCapacity;
import static java.lang.Math.max;
import static java.util.Arrays.asList;
import static java.util.Arrays.binarySearch;
import static java.util.Arrays.copyOf;
import static java.util.Objects.checkIndex;

/**
 * An {@link ArrayList} with
 * <a href="https://en.wikipedia.org/wiki/Copy-on-write">copy-on-write</a>
 * (CoW) semantics.
 * <p>
 * Upon instantiating this class, the backing array won't be copied immediately,
 * nor copied at every mutation. Rather, it's only referenced strongly, with
 * the list acting as a view to its elements by default; then, upon the first
 * modification request (e.g., a call to {@link #add(Object)}), the array is
 * copied and the strong reference to it is dropped.
 * <p>
 * As a consequence, changes to the array are only reflected on the list until
 * the first modification to the list. Conversely, changes to the list never
 * alter the array.
 * <p>
 * This class isn't thread-safe, as the backing array gets copied only one time
 * after the first write to the list; therefore, subsequent modifications to the
 * list are subject to the same race conditions of a standard, non-concurrent
 * {@link ArrayList}.
 * <p>
 * <b>NOTE</b>: The array gets copied even when the requested operation won't
 * change the list. {@code addAll(List.of())} adds nothing; yet, the array is
 * still copied.
 *
 * @author Jean Silva
 * @param <Element> An element of this list.
 * @see #addAll(Collection)
 * @see List#of()
 */
@SuppressLint("DiscouragedPrivateApi")
@SuppressWarnings("JavaReflectionMemberAccess")
public class OneTimeCopyOnWriteArrayList<Element> extends ArrayList<Element> {
  private Element[] view;
  private boolean isImmutableOrderedSetLike;
  private final int initialCapacity;

  private static final class SubList<Element>
    extends AbstractList<Element>
    implements RandomAccess {
    final Element[] view;
    final int startIndex;
    final int endIndex;

    SubList(
      final Element[] view,
      final int startIndex,
      final int endIndex
    ) throws ArrayIndexOutOfBoundsException {
      if (startIndex < 0)
        throw new ArrayIndexOutOfBoundsException(startIndex);
      if (startIndex > endIndex || endIndex > view.length)
        throw new ArrayIndexOutOfBoundsException(endIndex);
      this.view = view;
      this.startIndex = startIndex;
      this.endIndex = endIndex;
    }

    @Override
    public Element get(int index) {
      return view[startIndex + index];
    }

    @Override
    public int size() {
      return endIndex - startIndex;
    }
  }

  /**
   * Instantiates a {@link OneTimeCopyOnWriteArrayList}.
   *
   * @param initialCapacity Maximum amount of elements that the list will be
   *  able to hold until it grows or gets trimmed to its size, after its backing
   *  array has been copied.
   * @see #trimToSize()
   * @throws IllegalArgumentException If the initial capacity is negative.
   */
  public OneTimeCopyOnWriteArrayList(final int initialCapacity)
    throws IllegalArgumentException {
    if (initialCapacity < 0)
      throw new IllegalArgumentException(
        // the message is quite odd, but that's how this exception is thrown
        // when calling `super(int)` with a negative capacity as of Java 17.
        // we'll keep it that way here for mere consistency.
        "Illegal Capacity: " + initialCapacity
      );
    this.isImmutableOrderedSetLike = false;
    this.initialCapacity = initialCapacity;
  }

  /**
   * Instantiates a {@link OneTimeCopyOnWriteArrayList} whose view contains
   * incomparable, equal or unsorted elements. In such a list, elements will be
   * indexed linearly (rather than through binary search).
   *
   * @param view The backing array.
   * @see #OneTimeCopyOnWriteArrayList(Object[], boolean)
   */
  public OneTimeCopyOnWriteArrayList(final Element[] view) {
    this(view, /* isImmutableOrderedSetLike = */ false);
  }

  /**
   * Instantiates a {@link OneTimeCopyOnWriteArrayList}.
   *
   * @param view The backing array.
   * @param isImmutableOrderedSetLike Whether the view will remain unchanged
   *   throughout the list's lifetime and contains only comparable, distinct
   *   elements which are already sorted. This being {@code true} enables
   *   indexing the view by the list through binary search (as opposed to
   *   linearly).
   *   <p>
   *   The immutability, comparability and duplicate-free aspects are
   *   invariants, and ensuring they're satisfied is a responsibility of the
   *   caller. A one-time CoW list employs minimal to zero checking on whether
   *   these assumptions hold true, and violating them may result in incorrect
   *   indexing.
   * @see Comparable
   */
  public OneTimeCopyOnWriteArrayList(
    final Element[] view,
    final boolean isImmutableOrderedSetLike
  ) {
    this.view = view;
    this.isImmutableOrderedSetLike = isImmutableOrderedSetLike;
    this.initialCapacity = view.length;
  }

  @Override
  public boolean add(final Element element) {
    copyViewToBufferIdempotently();
    return super.add(element);
  }

  @Override
  public boolean addAll(final int index, Collection<? extends Element> c) {
    copyViewToBufferIdempotently();
    return super.addAll(index, c);
  }

  @Override
  public boolean addAll(final Collection<? extends Element> c) {
    copyViewToBufferIdempotently();
    return super.addAll(c);
  }

  @Override
  public void clear() {
    copyViewToBufferIdempotently();
    super.clear();
  }

  @Override
  public Object clone() {
    return view == null ? super.clone() : subList(0, view.length);
  }

  @Override
  public boolean contains(final Object o) {
    return indexOf(o) >= 0;
  }

  @Override
  public boolean equals(final Object o) {
    if (view == null)
      return super.equals(o);
    else if (o instanceof List<?> typedO)
      return view.length == typedO.size() && asList(view).equals(typedO);
    else
      return false;
  }

  @Override
  public Element get(final int index) throws IndexOutOfBoundsException {
    return view == null ? super.get(index) : view[index];
  }

  @Override
  public Element getFirst() throws NoSuchElementException {
    if (view == null)
      return super.getFirst();
    else if (view.length == 0)
      throw new NoSuchElementException();
    else
      return view[0];
  }

  @Override
  public Element getLast() throws NoSuchElementException {
    if (view == null)
      return super.getLast();
    else if (view.length == 0)
      throw new NoSuchElementException();
    else
      return view[view.length - 1];
  }

  @Override
  public int hashCode() {
    return view == null ? super.hashCode() : Arrays.hashCode(view);
  }

  @Override
  public int indexOf(final Object o) {
    if (view == null)
      return super.indexOf(o);
    if (isImmutableOrderedSetLike)
      try { return max(binarySearch(view, o), -1); }
      catch (final ClassCastException | IllegalArgumentException exception) {
        // welp! we were lied to… :(
        isImmutableOrderedSetLike = false;
      }
    for (int index = 0; index < view.length; index++) {
      final Element element = view[index];
      if (element == o || element.equals(o))
        return index;
    }
    return -1;
  }

  @Override
  public Iterator<Element> iterator() {
    return view == null ? super.iterator() : Arrays.stream(view).iterator();
  }

  @Override
  public int lastIndexOf(final Object o) {
    if (view == null)
      return super.lastIndexOf(o);
    if (isImmutableOrderedSetLike)
      return indexOf(o);
    for (int index = view.length - 1; index >= 0; index--)
      if (view[index].equals(o))
        return index;
    return -1;
  }

  @Override
  public ListIterator<Element> listIterator() {
    return view == null ? super.listIterator() : asList(view).listIterator();
  }

  @Override
  public ListIterator<Element> listIterator(final int index) {
    return view == null
      ? super.listIterator(index)
      : asList(view).listIterator(index);
  }

  @Override
  public Element remove(final int index) throws IndexOutOfBoundsException {
    copyViewToBufferIdempotently();
    return super.remove(index);
  }

  @Override
  public boolean remove(final Object o) {
    if (isImmutableOrderedSetLike && !contains(o))
      return false;
    copyViewToBufferIdempotently();
    return super.remove(o);
  }

  @Override
  public boolean removeAll(final Collection<?> c) {
    if (isImmutableOrderedSetLike && c.size() <= view.length) {
      boolean mayCopy = false;
      for (final Object element: c)
        if (contains(element)) {
          mayCopy = true;
          break;
        }
      if (!mayCopy)
        return false;
    }
    copyViewToBufferIdempotently();
    return super.removeAll(c);
  }

  @Override
  public Element removeFirst() throws NoSuchElementException {
    if (view == null)
      return super.removeFirst();
    else if (view.length == 0)
      throw new NoSuchElementException();
    else {
      copyViewToBufferIdempotently();
      return super.removeFirst();
    }
  }

  @Override
  public boolean removeIf(final Predicate<? super Element> filter)
    throws NullPointerException {
    if (view == null)
      return super.removeIf(filter);
    else if (view.length == 0)
      return false;
    else {
      copyViewToBufferIdempotently();
      return super.removeIf(filter);
    }
  }

  @Override
  public Element removeLast() throws NoSuchElementException {
    if (view == null)
      return super.removeLast();
    else if (view.length == 0)
      throw new NoSuchElementException();
    else {
      copyViewToBufferIdempotently();
      return super.removeLast();
    }
  }

  @Override
  protected void removeRange(final int fromIndex, final int toIndex)
    throws IndexOutOfBoundsException {
    if (view == null)
      super.removeRange(fromIndex, toIndex);
    else if (fromIndex < 0)
      throw new ArrayIndexOutOfBoundsException(fromIndex);
    else if (toIndex >= view.length)
      throw new ArrayIndexOutOfBoundsException(toIndex);
    else {
      copyViewToBufferIdempotently();
      super.removeRange(fromIndex, toIndex);
    }
  }

  @Override
  public Element set(final int index, final Element element)
    throws IndexOutOfBoundsException {
    if (view != null) {
      checkIndex(index, view.length);
      copyViewToBufferIdempotently();
    }
    return super.set(index, element);
  }

  @Override
  public int size() {
    return view == null ? super.size() : view.length;
  }

  @Override
  public Spliterator<Element> spliterator() {
    return view == null
      ? super.spliterator()
      : Arrays.stream(view).spliterator();
  }

  @Override
  public List<Element> subList(final int fromIndex, final int toIndex)
    throws IndexOutOfBoundsException {
    return view == null
      ? super.subList(fromIndex, toIndex)
      : new SubList<>(view, fromIndex, toIndex);
  }

  @Override
  public Object[] toArray() {
    return view == null ? super.toArray() : copyOf(view, view.length);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T[] toArray(final T[] a) throws NullPointerException {
    if (view == null)
      return super.toArray(a);
    if (a == null)
      throw new NullPointerException("a");
    final T[] result = (T[]) copyOf(view, a.length, a.getClass());
    if (view.length < result.length)
      result[view.length] = null;
    return result;
  }

  @SuppressWarnings("CatchMayIgnoreException")
  private void copyViewToBufferIdempotently() {
    if (view == null)
      return;
    boolean[] didCopyEfficiently = new boolean[1];
    try {
      withBuffer((bufferField, buffer) -> {
        // reading the buffer is already dangerous, and we'll go one step
        // further: re-set it and adapt the size of the list to its length.
        // if our recklessness throws an exception, we'll do it less
        // efficiently—but safely.
        try { bufferField.set(this, new Object[initialCapacity]); }
        catch (final IllegalAccessException cause) {
          throw new RuntimeException(cause);
        }
        Field sizeField = null;
        try { sizeField = ArrayList.class.getDeclaredField("size"); }
        catch (final NoSuchFieldException exception) {}
        if (sizeField != null) {
          final boolean wasSizeAccessible = sizeField.isAccessible();
          if (!wasSizeAccessible)
            try { sizeField.setAccessible(true); }
            catch (final RuntimeException exception) {}
          try {
            sizeField.set(this, initialCapacity);
            didCopyEfficiently[0] = true;
          } catch (final IllegalAccessException exception) {
            didCopyEfficiently[0] = false;
          }
          try { sizeField.setAccessible(wasSizeAccessible); }
          catch (final RuntimeException cause) {
            throw new SecurityException(cause);
          }
        }

        // something went wrong along the way… well, let us put the previous
        // buffer we've captured back into its place, as if nothing's changed.
        // we'll go through the fallback.
        if (!didCopyEfficiently[0])
          try { bufferField.set(this, buffer); }
          catch (final IllegalAccessException cause) {
            throw new RuntimeException(cause);
          }

        if (view == null)
          return;
        System.arraycopy(
          /* src = */     view,
          /* srcPos = */  0,
          /* dest = */    buffer,
          /* destPos = */ 0,
          /* length = */  buffer.length
        );
      });
    } catch (final RuntimeException exception) {
      assert exception instanceof SecurityException
          || exception.getCause() instanceof IllegalAccessException
           : "couldn't copy view to buffer";
      didCopyEfficiently[0] = false;
    }

    if (!didCopyEfficiently[0]) {
      // not so great: the default initial capacity may be different from the
      // user-defined one; if so, we'll have to shrink or grow. this is one of
      // the inefficiencies I alluded to.
      reserveExactCapacity(this, initialCapacity);
      super.addAll(asList(view));
    }

    view = null;
    isImmutableOrderedSetLike = false;
  }

  @SuppressWarnings("unchecked")
  private void withBuffer(final BiConsumer<Field, Element[]> action)
    throws SecurityException {
    Field bufferField = null;
    try { bufferField = ArrayList.class.getDeclaredField("elementData"); }
    catch (final NoSuchFieldException exception) {
      assert false : "buffer not found";
    }
    final boolean wasBufferAccessible = bufferField.isAccessible();
    if (!wasBufferAccessible)
      try { bufferField.setAccessible(true); }
      catch (final RuntimeException cause) {
        throw new SecurityException(cause);
      }
    Object[] buffer = new Object[0];
    try { buffer = (Object[]) bufferField.get(this); }
    catch (final IllegalAccessException exception) {
      assert false : "buffer was found, but is inaccessible";
    }
    action.accept(bufferField, (Element[]) buffer);
    bufferField.setAccessible(wasBufferAccessible);
  }
}