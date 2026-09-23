package com.jeanbarrossilva.dias.core;

import org.agrona.collections.IntHashSet;
import org.jetbrains.annotations.NotNull;

/** Extensions for {@link IntHashSet}s. */
public class IntHashSets {
  private IntHashSets() {}

  /**
   * Instantiates a set containing a single integer.
   *
   * @param value Only integer in the set.
   */
  @NotNull
  public static IntHashSet of(final int value) {
    final var self = new IntHashSet(/* proposedCapacity = */ 1);
    self.add(value);
    return self;
  }

  /**
   * Instantiates a set containing the given integers.
   *
   * @param values Integers to be in the set.
   */
  @NotNull
  public static IntHashSet of(final int ...values) {
    final var self = new IntHashSet(/* proposedCapacity = */ values.length);
    for (final int value: values)
      self.add(value);
    return self;
  }
}