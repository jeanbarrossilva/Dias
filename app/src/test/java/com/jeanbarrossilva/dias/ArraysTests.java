package com.jeanbarrossilva.dias;

import org.junit.Test;

import static com.jeanbarrossilva.dias.Arrays.getIndices;
import static org.assertj.core.api.Assertions.assertThat;

public class ArraysTests {
  @Test
  public void getsIndices() {
    assertThat(getIndices(new Object[0])).isEmpty();
    assertThat(getIndices(new Object[]{new Object(), new Object()}))
      .containsExactly(0, 1);
  }
}