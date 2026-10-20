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

import java.util.Comparator;
import java.util.function.Function;

/** Extensions for arrays. */
public final class Arrays {
  private Arrays() {}

  /**
   * Finds the index of an element using binary search, resorting to the result
   * of the given transform on such element and another element of the array to
   * compare the two. This is an efficient equivalent of mapping the array to
   * a list of {@code Key}s and binarily searching for the key on that list.
   *
   * @param self Array whose transformed elements will be compared.
   * @param key Transformation result being searched for.
   * @param comparator Comparator for the key and a transformed element of the
   *   array. If this is {@code null}, they'll be compared by their natural
   *   order.
   * @param transform Transforms an element of the array.
   * @param <Element> An element of the array.
   * @param <Key> Object being searched for, into which an element of the array
   *   may be transformed.
   * @return The index at which the given key is, or, in case it's not in the
   *   array, the insertion point for such key in the array (as described in the
   *   documentation of {@link
   *   java.util.Arrays#binarySearch(Object[], Object)}).
   * @see Comparator#naturalOrder()
   * @throws ClassCastException If the key isn't comparable to an element of the
   *   array.
   * @throws NullPointerException If either the key or an element to which the
   *   key was compared was {@code null} and the comparator used doesn't support
   *   comparing {@code null}s.
   */
  @SuppressWarnings("unchecked")
  public static <Element extends Comparable<Element>, Key> int binarySearch(
    final Element[] self,
    final Key key,
    final Comparator<Key> comparator,
    final Function<Element, Key> transform
  ) throws ClassCastException, NullPointerException {
    int ins = -1;
    if (self != null) {
      ins = self.length;
      if (transform != null) {
        int lo = 0;
        int hi = self.length - 1;
        while (hi - lo >= 0) {
          final int mid = (lo + hi) >>> 1;
          final Key img = transform.apply(self[mid]);
          final int cmp =
            (comparator == null
              ? (Comparator<Key>) Comparator.naturalOrder()
              : comparator)
              .compare(key, img);
          if (cmp < 0)
            hi = mid - 1;
          else if (cmp == 0)
            return mid;
          else
            lo = mid + 1;
        }
        ins = -lo - 1;
      }
    }
    return ins;
  }
}