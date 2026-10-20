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
import java.util.List;

/** Extensions for {@link Comparator}s. */
public final class Comparators {
  private Comparators() {}

  /**
   * Instantiates a comparator that compares objects according to their
   * respective index in the list. E.g., given a list with 3 or more elements,
   * the first element is lesser than the second, which is lesser than the
   * third, and so forth.
   *
   * @author Bubletan
   * @author Stuart Marks
   * @param ordering List containing the elements to be compared by index.
   * @param <Element> An element of the list.
   */
  public static <Element> Comparator<Element> comparingByIndex(
    final List<? extends Element> ordering
  ) {
    // Copyright © 2016 Bubletan, Stuart Marks
    // https://stackoverflow.com/a/41128993/10252241
    //
    // This work is licensed under the Creative Commons Attribution-ShareAlike
    // 3.0 Unported License. For a copy of this license, refer to
    // https://creativecommons.org/licenses/by-sa/3.0.

    // Dias-specific changes:
    // • Check whether `ordering` is null;
    // • reformat code; and
    // • rename comparator's arguments to `a` and `b`.
    if (ordering == null)
      return null;
    return (a, b) -> Integer
      .compareUnsigned(ordering.indexOf(a), ordering.indexOf(b));
  }
}