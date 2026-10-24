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
   * {@code count} when this function returns. For a trimmed copy of the
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
   * Produces a typed copy of the unordered union of the two given arrays.
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
   * Produces the unordered union of the two given arrays.
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
    System.arraycopy(
      /* src = */     self,
      /* srcPost = */ 0,
      /* dest = */    result,
      /* destPos = */ 0,
      /* length = */  self.length
    );
    for (int i = 0; i < other.length; i++) {
      final Object element = other[i];
      int j = binarySearch(result, 0, self.length + i, element);
      if (j < 0)
        j = -j - 1;
      System.arraycopy(
        /* src = */     result,
        /* srcPos = */  j,
        /* dest = */    result,
        /* destPos = */ j + 1,
        /* length = */  self.length + i - j
      );
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
}