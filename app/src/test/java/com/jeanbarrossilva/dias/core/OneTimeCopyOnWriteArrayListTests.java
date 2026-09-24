package com.jeanbarrossilva.dias.core;

import org.junit.Test;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.INTEGER;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

public class OneTimeCopyOnWriteArrayListTests {
  @Test
  public void defaultInitialCapacityIsEqualToThatOfItsSuperclass() {
    assertThat(new OneTimeCopyOnWriteArrayList<>())
      .asInstanceOf(type(OneTimeCopyOnWriteArrayList.class))
      .extracting("initialCapacity", as(INTEGER))
      .isEqualTo(OneTimeCopyOnWriteArrayList.DEFAULT_INITIAL_CAPACITY);
  }

  @Test
  public void throwsWhenInitialCapacityIsNegative() {
    assertThatThrownBy(() -> new OneTimeCopyOnWriteArrayList<>(-1))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("initialCapacity (-1) < 0");
  }

  @Test
  public void isViewToBackingArrayPriorToModifications() {
    final var view = new Object[]{new Object(), new Object()};
    final var list = new OneTimeCopyOnWriteArrayList<>(view);
    assertThat(list).containsExactly(view);
  }
}