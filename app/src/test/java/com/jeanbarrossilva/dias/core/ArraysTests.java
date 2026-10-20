package com.jeanbarrossilva.dias.core;

import org.junit.Test;
import org.junit.runners.Suite;

import static com.jeanbarrossilva.dias.core.Arrays.binarySearch;
import static org.assertj.core.api.Assertions.assertThat;

@Suite.SuiteClasses(ArraysTests.BinarySearchTests.class)
public final class ArraysTests {
  public final static class BinarySearchTests {
    @Test
    public void returnsLengthMinusOneWhenKeyIsNotFound() {
      assertThat(
        binarySearch(new Integer[0], 0, /* comparator = */ null, n -> n)
      )
        .isEqualTo(-1);
      assertThat(
        binarySearch(new Integer[]{1, 2}, 3, /* comparator = */ null, n -> n)
      )
        .isEqualTo(-3);
    }
  }
}