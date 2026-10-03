package com.jeanbarrossilva.dias.core;

import org.agrona.collections.IntHashSet;
import org.junit.Test;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.INTEGER;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

public final class IntHashSetsTests {
  @Test
  public void instantiatesSetWithSingleValue() {
    assertThat(IntHashSets.of(0))
      .containsExactly(0)
      .asInstanceOf(type(IntHashSet.class))
      .extracting(IntHashSet::capacity, as(INTEGER))
      .isEqualTo(IntHashSet.DEFAULT_INITIAL_CAPACITY);
  }
}