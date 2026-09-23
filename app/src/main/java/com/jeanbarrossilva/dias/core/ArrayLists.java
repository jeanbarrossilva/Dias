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

import androidx.annotation.NonNull;

import java.util.ArrayList;

/**
 * Extensions for {@link ArrayList}s.
 *
 * @author Jean Silva
 */
public final class ArrayLists {
  private ArrayLists() {}

  /**
   * Sets the given amount as the capacity of the list.
   * <p>
   * After a call to this function, the list's capacity is <i>guaranteed</i> to
   * be the specified one. This also denotes that, if the given capacity is
   * lesser than the list's size, the last
   * |{@code capacity} - {@code self.size()}| elements will be dropped.
   *
   * @param self List whose capacity will be set.
   * @param capacity The new capacity of the list. This function is a no-op if
   *                 this is negative.
   */
  public static void reserveExactCapacity(
    @NonNull final ArrayList<?> self,
    final int capacity
  ) {
    if (capacity < 0)
      return;
    if (!self.isEmpty())
      if (capacity == 0)
        self.clear();
      else if (self.size() <= capacity)
        self.ensureCapacity(capacity);
      else
        while (self.size() > capacity)
          self.removeLast();
    self.trimToSize();
  }
}