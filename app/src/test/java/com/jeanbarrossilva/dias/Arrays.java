package com.jeanbarrossilva.dias;

import androidx.annotation.NonNull;

import com.google.common.collect.ContiguousSet;

import java.lang.reflect.Array;
import java.util.SequencedSet;

/** Extensions for {@link Array}s. */
public class Arrays {
  /**
   * Returns the indices of the array.
   *
   * @param self Array whose indices will be returned.
   */
  @NonNull
  public static SequencedSet<Integer> getIndices(@NonNull final Object[] self) {
    return self.length == 0
      ? ContiguousSets.withoutIntegers
      : ContiguousSet.closedOpen(0, self.length);
  }
}