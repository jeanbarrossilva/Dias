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

package com.jeanbarrossilva.dias.core;

import static java.lang.System.arraycopy;
import static java.util.Arrays.binarySearch;
import static java.util.Arrays.copyOf;

/** Extensions for arrays. */
public final class Arrays {
  private Arrays() {}

  /**
   * Produces a trimmed copy of the difference between the two given arrays. The
   * given arrays are expected to be sorted, with comparable elements;
   * otherwise, the behavior of this function is undefined.
   * <p>
   * For an uncopied and untrimmed difference, refer to {@link
   * #difference(Object[], Object[], int[])}.
   *
   * @param <Element> An element of both arrays.
   * @param self Array to compare to {@code other}.
   * @param other Array to be compared to {@code self}.
   * @param type Class of the resulting array.
   * @throws ClassCastException If an element of {@code self} can't be compared
   *   to another of {@code other} or vice versa.
   * @return The difference between both arrays, or {@code null} in case either
   *   array or {@code type} is {@code null}.
   */
  public static <Element> Element[] difference(
    final Element[] self,
    final Element[] other,
    final Class<Element[]> type
  ) throws ClassCastException {
    if (type == null)
      return null;
    final var length = new int[1];
    final var untrimmedDiff = difference(self, other, length);
    return untrimmedDiff == null
      ? null
      : copyOf(untrimmedDiff, /* length = */ length[0], type);
  }

  /**
   * Produces an array containing elements in {@code self} and not in {@code
   * other}. Both arrays are expected to be sorted, with comparable elements;
   * otherwise, the behavior of this function is undefined.
   * <p>
   * The returned array will have the same length as {@code self}. However, the
   * difference modulo may be lesser, and will be set as the first element of
   * {@code length} when this function returns. For a trimmed copy of the
   * difference, refer to {@link #difference(Object[], Object[], Class)}.
   *
   * @param self Array to compare to {@code other}.
   * @param other Array to be compared to {@code self}.
   * @param length Holder of the <i>actual</i> amount of elements that are in
   *   {@code self} and not in {@code other}, which will be put into the first
   *   index of {@code length} in case {@code length.length} ≥ 1. If so, any
   *   element at an index ≥ {@code length[0]} will be {@code null} in the
   *   returned array.
   * @throws ClassCastException If an element of {@code self} can't be compared
   *   to another of {@code other} or vice versa.
   * @return The difference between both arrays, or {@code null} in case either
   *   array is {@code null}.
   */
  public static Object[] difference(
    final Object[] self,
    final Object[] other,
    final int[] length
  ) throws ClassCastException {
    if (self == null || other == null)
      return null;
    if (self.length == 0 || other.length == 0) {
      if (!isNullOrEmpty(length))
        length[0] = self.length;
      return self;
    }
    final Object[] result = new Object[self.length];
    int index = 0;
    for (final Object element: self)
      if (binarySearch(other, element) < 0)
        result[index++] = element;
    if (!isNullOrEmpty(length))
      length[0] = index;
    return result;
  }

  /**
   * Produces a trimmed copy of the intersection between the two given arrays.
   * The given arrays are expected to be sorted, with only comparable elements;
   * otherwise, the behavior of this function is undefined.
   * <p>
   * For an untrimmed copy, refer to {@link
   * #intersection(Object[], Object[], int[])}.
   *
   * @param <Element> An element of both arrays.
   * @param self Array to intersect with {@code other}.
   * @param other Array to be intersected with {@code self}.
   * @param type Class of the resulting array.
   * @return The intersection between both arrays, or {@code null} in case
   *   either array or {@code type} is {@code null}.
   * @throws ClassCastException If an element of {@code self} can't be compared
   *   to another of {@code other} or vice versa.
   */
  public static <Element> Element[] intersection(
    final Element[] self,
    final Element[] other,
    final Class<Element[]> type
  ) throws ClassCastException {
    if (type == null)
      return null;
    final var length = new int[1];
    final var untrimmedIntersection =
      intersection(self, other, length);
    return untrimmedIntersection == null
      ? null
      : copyOf(untrimmedIntersection, length[0], type);
  }

  /**
   * Produces an array containing only elements present on both given arrays.
   * The given arrays are expected to be sorted, with only comparable elements;
   * otherwise, the behavior of this function is undefined.
   * <p>
   * The returned array will have the same length as the greatest of the two
   * arrays. However, the amount of elements in common may be lesser, and will
   * be held by the {@code length} when this function returns.
   *
   * @param self Array to intersect with {@code other}.
   * @param other Array to be intersected with {@code self}.
   * @param length Holder of the <i>actual</i> amount of elements that both
   *   arrays have in common. The amount will be put into the first index if
   *   {@code count.length} ≥ 1.
   * @return The intersection between both arrays, or {@code null} in case
   *   either array is {@code null}.
   * @throws ClassCastException If an element of {@code self} can't be compared
   *   to another of {@code other} or vice versa.
   */
  public static Object[] intersection(
    final Object[] self,
    final Object[] other,
    final int[] length
  ) throws ClassCastException {
    if (self == null || other == null)
      return null;
    if (self.length == 0) {
      if (!isNullOrEmpty(length))
        length[0] = 0;
      return self;
    }
    if (other.length == 0) {
      if (!isNullOrEmpty(length))
        length[0] = 0;
      return other;
    }
    final Object[] greatest = self.length >= other.length ? self : other;
    final Object[] smallest = greatest == other ? self : other;
    final Object[] result = new Object[greatest.length];
    int additionCount = 0;
    for (final Object element: smallest)
      if (binarySearch(greatest, element) >= 0)
        result[additionCount++] = element;
    if (!isNullOrEmpty(length))
      length[0] = additionCount;
    return result;
  }

  /**
   * Produces a typed copy of the sorted union of the two given arrays.
   * <p>
   * If {@code self} is not {@code null}, it is assumed to be sorted, have
   * comparable elements and none in common with the other array; in case one of
   * these conditions isn't satisfied, the behavior of this function is
   * undefined.
   * <p>
   * For an uncopied and type-erased union, refer to {@link
   * #union(Object[], Object[])}.
   *
   * @param <Element> An element of both arrays.
   * @param self Array to which {@code other} will be joined.
   * @param other Array to be joined to {@code self}.
   * @param type Class of the resulting array.
   * @throws ClassCastException If an element of {@code self} can't be compared
   *   to another of {@code other} or vice versa.
   * @return A union of the two arrays, sorted and of length {@code
   *   self.length} + {@code other.length}.
   */
  public static <Element> Element[] union(
    final Element[] self,
    final Element[] other,
    final Class<Element[]> type
  ) throws ClassCastException {
    if (type == null)
      return null;
    final Object[] typeErasedUnion = union(self, other);
    return typeErasedUnion == null
      ? null
      : copyOf(typeErasedUnion, typeErasedUnion.length, type);
  }

  /**
   * Produces the sorted union of the two given arrays.
   * <p>
   * This function assumes that both arrays contain comparable elements; that
   * {@code self}, if not {@code null}, is sorted; and that its elements differ
   * from those of the other array (i.e., {@code self} \ {@code other} = {@code
   * self}). In case these conditions aren't satisfied, the behavior of this
   * function is undefined.
   *
   * @param self Array to which {@code other} will be joined.
   * @param other Array to be joined to {@code self}.
   * @return A union of the two arrays, sorted and of length {@code
   *   self.length} + {@code other.length}.
   * @see #difference( Object[], Object[], Class)
   * @throws ClassCastException If an element of {@code self} can't be compared
   *   to another of {@code other} or vice versa.
   */
  public static Object[] union(final Object[] self, final Object[] other)
    throws ClassCastException {
    if (self == null || other == null)
      return null;
    if (self.length == 0)
      return other;
    if (other.length == 0)
      return self;
    final Object[] result = new Object[self.length + other.length];
    copy(result, self);
    for (int i = 0; i < other.length; i++) {
      final Object element = other[i];
      int j = binarySearch(result, 0, self.length + i, element);
      if (j < 0)
        j = -j - 1;
      offset(result, /* from = */ j, /* to = */ self.length + i, /* by = */ 1);
      result[j] = element;
    }
    return result;
  }

  /**
   * Returns whether the given array is {@code null} or contains no elements.
   *
   * @param self Array to check.
   */
  public static boolean isNullOrEmpty(final int[] self) {
    return self == null || self.length == 0;
  }

  /**
   * Copies all elements of {@code other} to {@code self}, starting from the
   * beginning of the two arrays, such that
   * {@code Arrays.copyOfRange(self, 0, other.length)} contains every element of
   * {@code other} in order.
   *
   * @param self Array to which the other will be copied.
   * @param other Array to be copied to the beginning of {@code self}.
   * @see java.util.Arrays#copyOfRange(Object[], int, int)
   * @throws IndexOutOfBoundsException If the other array doesn't fit into
   *   {@code self}.
   * @throws NullPointerException If either array is {@code null}.
   */
  public static void copy(final Object[] self, final Object[] other) {
    arraycopy(
      /* src = */     other,
      /* srcPos = */  0,
      /* dest = */    self,
      /* destPos = */ 0,
      /* length = */  other.length
    );
  }

  /**
   * Moves a portion of the array by the given offset.
   *
   * @param self Array whose portion will be moved.
   * @param from Start index of the portion to be moved (inclusive).
   * @param to End index of the portion to be moved (exclusive).
   * @param by The offset; the amount of indices to move the portion by.
   * @throws IndexOutOfBoundsException If the start index is lesser than the end
   *   one; either index is outside the array's bounds; or the portion's length
   *   (i.e., {@code to} - {@code from}) is greater than
   *   {@code self.length} - {@code from}.
   * @throws NullPointerException If the array is {@code null}.
   */
  public static void offset(
    final Object[] self,
    final int from,
    final int to,
    final int by
  ) throws IndexOutOfBoundsException, NullPointerException {
    arraycopy(
      /* src = */     self,
      /* srcPos = */  from,
      /* dest = */    self,
      /* destPos = */ from + by,
      /* length = */  to - from
    );
  }
}