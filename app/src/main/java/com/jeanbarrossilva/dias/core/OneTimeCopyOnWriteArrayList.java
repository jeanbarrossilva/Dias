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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.Spliterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

import static com.jeanbarrossilva.dias.core.ArrayLists.reserveExactCapacity;
import static java.lang.Math.max;
import static java.util.Arrays.asList;
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
 * {@link ArrayList}. For thread-safety, see {@link CopyOnWriteArrayList}.
 *
 * @author Jean Silva
 * @param <Element> An element of this list.
 */
public class OneTimeCopyOnWriteArrayList<Element> extends ArrayList<Element> {
  private Element[] backingArray;
  private boolean isImmutableOrderedSetLike;
  private final int initialCapacity;

  private static final class SubList<Element>
    extends AbstractList<Element>
    implements RandomAccess {
    private final Element[] backingArray;
    private final int startIndex;
    private final int endIndex;

    SubList(
      final Element[] backingArray,
      final int startIndex,
      final int endIndex
    ) throws ArrayIndexOutOfBoundsException {
      if (startIndex < 0)
        throw new ArrayIndexOutOfBoundsException(startIndex);
      if (startIndex > endIndex || endIndex > backingArray.length)
        throw new ArrayIndexOutOfBoundsException(endIndex);
      this.backingArray = backingArray;
      this.startIndex = startIndex;
      this.endIndex = endIndex;
    }

    @Override
    public Element get(int index) throws ArrayIndexOutOfBoundsException {
      return backingArray[startIndex + index];
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
        "initialCapacity (" + initialCapacity + ") < 0"
      );
    this.isImmutableOrderedSetLike = false;
    this.initialCapacity = initialCapacity;
  }

  /**
   * Instantiates a {@link OneTimeCopyOnWriteArrayList} whose view contains
   * incomparable, equal or unsorted elements. In such a list, elements will be
   * indexed linearly (rather than through binary search).
   *
   * @param backingArray Array to which the list acts as a view until the first
   *   modification on the list; it's also the array to be copied upon such
   *   modification.
   * @see #OneTimeCopyOnWriteArrayList(Object[], boolean)
   */
  public OneTimeCopyOnWriteArrayList(final Element[] backingArray) {
    this(backingArray, /* isImmutableOrderedSetLike = */ false);
  }

  /**
   * Instantiates a {@link OneTimeCopyOnWriteArrayList}.
   *
   * @param backingArray Array to which the list acts as a view until the first
   *   modification on the list; it's also the array to be copied upon such
   *   modification.
   * @param isImmutableOrderedSetLike Whether the view will remain unchanged
   *   throughout the list's lifetime and contains only comparable, distinct
   *   elements which are already sorted. This being {@code true} enables
   *   indexing the view by the list through binary search (as opposed to
   *   linearly).
   *   <p>
   *   The immutability, comparability, duplicate-free and sorting aspects are
   *   invariants, and ensuring they're satisfied is a responsibility of the
   *   caller. A one-time CoW list employs minimal to zero checking on whether
   *   these assumptions hold true, and violating them may result in incorrect
   *   indexing.
   * @see Comparable
   */
  public OneTimeCopyOnWriteArrayList(
    final Element[] backingArray,
    final boolean isImmutableOrderedSetLike
  ) {
    this.backingArray = backingArray;
    this.isImmutableOrderedSetLike = isImmutableOrderedSetLike;
    this.initialCapacity = backingArray.length;
  }

  @Override
  public boolean add(final Element element) {
    if (backingArray != null)
      copyAndDereferenceBackingArray();
    return super.add(element);
  }

  @Override
  public boolean addAll(final int index, Collection<? extends Element> c) {
    if (backingArray != null) {
      checkIndex(index, backingArray.length + 1);
      copyAndDereferenceBackingArray();
    }
    return super.addAll(index, c);
  }

  @Override
  public boolean addAll(final Collection<? extends Element> c) {
    if (c.isEmpty())
      return false;
    if (backingArray != null)
      copyAndDereferenceBackingArray();
    return super.addAll(c);
  }

  @Override
  public void clear() {
    if (backingArray != null) {
      if (backingArray.length == 0)
        return;
      copyAndDereferenceBackingArray();
    }
    super.clear();
  }

  @Override
  public Object clone() {
    return backingArray == null
      ? super.clone()
      : new OneTimeCopyOnWriteArrayList<>(
        /* backingArray = */ copyOf(backingArray, backingArray.length),
        isImmutableOrderedSetLike
      );
  }

  @Override
  public boolean contains(final Object o) {
    return indexOf(o) >= 0;
  }

  @Override
  public boolean equals(final Object o) {
    if (backingArray == null)
      return super.equals(o);
    if (o instanceof List<?> typedO) {
      if (backingArray.length != typedO.size())
        return false;
      for (int index = 0; index < backingArray.length; index++)
        if (!Objects.equals(backingArray[index], typedO.get(index)))
          return false;
      return true;
    }
    return false;
  }

  @Override
  public Element get(final int index) throws IndexOutOfBoundsException {
    return backingArray == null ? super.get(index) : backingArray[index];
  }

  @Override
  public Element getFirst() throws NoSuchElementException {
    if (backingArray == null)
      return super.getFirst();
    if (backingArray.length == 0)
      throw new NoSuchElementException();
    return backingArray[0];
  }

  @Override
  public Element getLast() throws NoSuchElementException {
    if (backingArray == null)
      return super.getLast();
    if (backingArray.length == 0)
      throw new NoSuchElementException();
    return backingArray[backingArray.length - 1];
  }

  @Override
  public int hashCode() {
    return backingArray == null
      ? super.hashCode()
      : Arrays.hashCode(backingArray);
  }

  @Override
  @SuppressWarnings("CatchMayIgnoreException")
  public int indexOf(final Object o) {
    if (backingArray == null)
      return super.indexOf(o);
    if (isImmutableOrderedSetLike)
      try { return findIndexWithBinarySearch(o); }
      catch (final IllegalStateException exception) {}
    for (int index = 0; index < backingArray.length; index++)
      if (Objects.equals(backingArray[index], o))
        return index;
    return -1;
  }

  @Override
  public Iterator<Element> iterator() {
    return backingArray == null
      ? super.iterator()
      : Arrays.stream(backingArray).iterator();
  }

  @Override
  @SuppressWarnings("CatchMayIgnoreException")
  public int lastIndexOf(final Object o) {
    if (backingArray == null)
      return super.lastIndexOf(o);
    if (isImmutableOrderedSetLike)
      try { return findIndexWithBinarySearch(o); }
      catch (final IllegalStateException exception) {}
    for (int index = backingArray.length - 1; index >= 0; index--)
      if (Objects.equals(backingArray[index], o))
        return index;
    return -1;
  }

  @Override
  public ListIterator<Element> listIterator() {
    return backingArray == null
      ? super.listIterator()
      : asList(backingArray).listIterator();
  }

  @Override
  public ListIterator<Element> listIterator(final int index) {
    return backingArray == null
      ? super.listIterator(index)
      : asList(backingArray).listIterator(index);
  }

  @Override
  public Element remove(final int index) throws IndexOutOfBoundsException {
    if (backingArray != null) {
      checkIndex(index, backingArray.length);
      copyAndDereferenceBackingArray();
    }
    return super.remove(index);
  }

  @Override
  public boolean remove(final Object o) {
    if (isImmutableOrderedSetLike && !contains(o))
      return false;
    if (backingArray != null)
      copyAndDereferenceBackingArray();
    return super.remove(o);
  }

  @Override
  public boolean removeAll(final Collection<?> c) {
    if (isImmutableOrderedSetLike) {
      boolean mayCoW = false;
      for (final Object element: c)
        if (contains(element)) {
          mayCoW = true;
          break;
        }
      if (!mayCoW)
        return false;
    }
    if (backingArray != null)
      copyAndDereferenceBackingArray();
    return super.removeAll(c);
  }

  @Override
  public Element removeFirst() throws NoSuchElementException {
    if (backingArray == null)
      return super.removeFirst();
    if (backingArray.length == 0)
      throw new NoSuchElementException();
    copyAndDereferenceBackingArray();
    return super.removeFirst();
  }

  @Override
  public boolean removeIf(final Predicate<? super Element> filter)
    throws NullPointerException {
    if (backingArray != null) {
      if (backingArray.length == 0)
        return false;
      copyAndDereferenceBackingArray();
    }
    return super.removeIf(filter);
  }

  @Override
  public Element removeLast() throws NoSuchElementException {
    if (backingArray == null)
      return super.removeLast();
    if (backingArray.length == 0)
      throw new NoSuchElementException();
    copyAndDereferenceBackingArray();
    return super.removeLast();
  }

  @Override
  protected void removeRange(final int fromIndex, final int toIndex)
    throws IndexOutOfBoundsException {
    if (backingArray != null) {
      if (fromIndex < 0)
        throw new ArrayIndexOutOfBoundsException(fromIndex);
      if (toIndex >= backingArray.length)
        throw new ArrayIndexOutOfBoundsException(toIndex);
      copyAndDereferenceBackingArray();
    }
    super.removeRange(fromIndex, toIndex);
  }

  @Override
  public Element set(final int index, final Element element)
    throws IndexOutOfBoundsException {
    if (backingArray != null) {
      checkIndex(index, backingArray.length);
      copyAndDereferenceBackingArray();
    }
    return super.set(index, element);
  }

  @Override
  public int size() {
    return backingArray == null ? super.size() : backingArray.length;
  }

  @Override
  public Spliterator<Element> spliterator() {
    return backingArray == null
      ? super.spliterator()
      : Arrays.stream(backingArray).spliterator();
  }

  @Override
  public List<Element> subList(final int fromIndex, final int toIndex)
    throws IndexOutOfBoundsException {
    return backingArray == null
      ? super.subList(fromIndex, toIndex)
      : new SubList<>(backingArray, fromIndex, toIndex);
  }

  @Override
  public Object[] toArray() {
    return backingArray == null
      ? super.toArray()
      : copyOf(backingArray, backingArray.length);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T[] toArray(final T[] a) throws NullPointerException {
    if (backingArray == null)
      return super.toArray(a);
    if (a == null)
      throw new NullPointerException("a");
    final T[] result;
    if (backingArray.length <= a.length) {
      result =
        (T[]) copyOf(backingArray, backingArray.length + 1, a.getClass());
      if (backingArray.length < result.length)
        result[backingArray.length] = null;
    } else
      result = (T[]) copyOf(backingArray, a.length, a.getClass());
    return result;
  }

  private void copyAndDereferenceBackingArray() {
    // not so great: the default initial capacity may be different from the
    // user-defined one; if so, we'll have to shrink or grow.
    //
    // accessing the superclass' backing array and the superclass' size would
    // prevent us from having to do this, but these are implementation details,
    // and are so very finicky to meddle with; not worth the hassle whatsoever.
    reserveExactCapacity(this, initialCapacity);
    super.addAll(this);

    backingArray = null;
    isImmutableOrderedSetLike = false;
  }

  private int findIndexWithBinarySearch(final Object key)
    throws IllegalStateException {
    try { return max(Arrays.binarySearch(backingArray, key), -1); }
    catch (final ClassCastException | IllegalArgumentException cause) {
      // welp! we were lied to… :(
      isImmutableOrderedSetLike = false;
      throw new IllegalStateException(cause);
    }
  }
}