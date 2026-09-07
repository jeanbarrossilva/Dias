package com.jeanbarrossilva.dias;

import org.junit.Test;

import static com.jeanbarrossilva.dias.Objects.as;
import static org.assertj.core.api.Assertions.assertThat;

public final class ObjectsTests {
  @Test
  public void casts() {
    assertThat(as(Integer.class, null)).isNull();
    assertThat(as(Integer.class, new Object())).isNull();
    assertThat(as(Integer.class, 2)).isEqualTo(Integer.valueOf(2));
    assertThat(as(Number.class, 2)).isEqualTo(2);
  }
}