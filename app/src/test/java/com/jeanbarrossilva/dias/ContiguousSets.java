package com.jeanbarrossilva.dias;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;

/** Extensions for {@link ContiguousSet}s. */
public class ContiguousSets {
  /** An empty set of integers. */
  public static final ContiguousSet<Integer> withoutIntegers =
    ContiguousSet.create(
      Range.lessThan(Integer.MIN_VALUE),
      DiscreteDomain.integers()
    );
}