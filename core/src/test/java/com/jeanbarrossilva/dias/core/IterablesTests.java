package com.jeanbarrossilva.dias.core;

import org.junit.Test;

import java.util.List;

import static com.jeanbarrossilva.dias.core.Iterables.getIndices;
import static com.jeanbarrossilva.dias.core.Iterables.joinToString;
import static org.assertj.core.api.Assertions.assertThat;

public class IterablesTests {
  @Test
  public void getsIndices() {
    assertThat(getIndices(List.of())).isEmpty();
    assertThat(getIndices(List.of(1, 2))).containsExactly(0, 1);
  }

  @Test
  public void joinsToString() {
    assertThat(joinToString(List.of(), ", ", " and ")).isEqualTo("");
    assertThat(joinToString(List.of(0), ", ", " and ")).isEqualTo("0");
    assertThat(joinToString(List.of(0, 1), ", ", " and ")).isEqualTo("0 and 1");
    assertThat(joinToString(List.of(0, 1, 2), ", ", " and "))
      .isEqualTo("0, 1 and 2");
  }
}