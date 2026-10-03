package com.jeanbarrossilva.dias.core;

import org.agrona.collections.IntArrayList;

/** Extensions for {@link IntArrayList}. */
public class IntArrayLists {
  private IntArrayLists() {}

  /**
   * Instantiates a set containing the given integers.
   *
   * @param values Integers to be in the set.
   */
  public static IntArrayList of(final int ...values) {
    if (values == null)
      return null;
    final var self = new IntArrayList(
      /* initialCapacity = */ values.length,
                              IntArrayList.DEFAULT_NULL_VALUE
    );
    for (final int value: values)
      self.add(value);
    return self;
  }
}