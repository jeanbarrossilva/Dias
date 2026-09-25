package com.jeanbarrossilva.dias.core;

import androidx.annotation.NonNull;

import org.agrona.collections.IntArrayList;

/** Extensions for {@link IntArrayList}. */
public class IntArrayLists {
  private IntArrayLists() {}

  /**
   * Instantiates a set containing the given integers.
   *
   * @param values Integers to be in the set.
   */
  @NonNull
  public static IntArrayList of(final int ...values) {
    final var self = new IntArrayList(
      /* initialCapacity = */ values.length,
                              IntArrayList.DEFAULT_NULL_VALUE
    );
    for (final int value: values)
      self.add(value);
    return self;
  }
}