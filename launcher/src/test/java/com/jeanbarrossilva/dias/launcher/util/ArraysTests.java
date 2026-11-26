/*
 * Copyright © 2026 Jean Silva
 *
 * Dias is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 2 of the License, or (at your option) any later
 * version.
 *
 * Dias is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Dias. If not, see https://www.gnu.org/licenses.
 */

package com.jeanbarrossilva.dias.launcher.util;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static com.jeanbarrossilva.dias.launcher.util.Arrays.copyOf;
import static com.jeanbarrossilva.dias.launcher.util.Arrays.difference;
import static com.jeanbarrossilva.dias.launcher.util.Arrays.indexOf;
import static com.jeanbarrossilva.dias.launcher.util.Arrays.intersection;
import static com.jeanbarrossilva.dias.launcher.util.Arrays.isNullOrEmpty;
import static com.jeanbarrossilva.dias.launcher.util.Arrays.union;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Enclosed.class)
public final class ArraysTests {
  public static final class CopyOfTests {
    @Test
    public void copies() {
      final var self = new Integer[]{1, 2, 3};
      assertThat(copyOf(self))
        .isNotSameAs(self)
        .containsExactly(self);
    }
  }

  public static final class DifferenceWithBinarySearchTests {
    @Test
    public void returnsNullWhenEitherArrayIsNull() {
      assertThat(difference(null, new Integer[0], Integer[].class))
        .isNull();
      assertThat(difference(new Integer[0], null, Integer[].class))
        .isNull();
      assertThat(difference(null, null, Integer[].class))
        .isNull();
    }

    @Test
    public void returnsNullWhenTypeIsNull() {
      assertThat(
        difference(new Integer[0], new Integer[0], (Class<Integer[]>) null)
      )
        .isNull();
    }

    @Test
    public void returnsSelfWhenOtherIsEmpty() {
      final Integer[] self = {1, 2, 3};
      assertThat(difference(self, new Integer[0], Integer[].class))
        .isEqualTo(self);
    }

    @Test
    public void returnsSelfWhenSelfIsEmpty() {
      final Integer[] self = {};
      assertThat(difference(self, new Integer[]{4, 5, 6}, Integer[].class))
        .isEqualTo(self);
    }

    @Test
    public void computesDifference() {
      assertThat(
        difference(
          new Integer[]{1, 2, 3, 6},
          new Integer[]{2, 3, 4, 5},
          Integer[].class
        )
      )
        .containsExactly(1, 6);
    }
  }

  public static final class IndexOfTests {
    @Test
    public void returnsNegativeOneWhenArrayIsNull() {
      assertThat(indexOf(null, 0)).isEqualTo(-1);
    }

    @Test
    public void returnsNegativeOneWhenKeyIsNotFound() {
      assertThat(indexOf(new Integer[]{1, 2, 3}, 4)).isEqualTo(-1);
    }

    @Test
    public void returnsIndexOfKeyInTheArray() {
      assertThat(indexOf(new Integer[]{1, 2, 3}, 2)).isEqualTo(1);
    }
  }

  public static final class IntersectionTests {
    @Test
    public void returnsNullWhenEitherArrayIsNull() {
      assertThat(intersection(null, new Integer[0], Integer[].class)).isNull();
      assertThat(intersection(new Integer[0], null, Integer[].class)).isNull();
    }

    @Test
    public void returnsSelfWhenSelfIsEmpty() {
      final var self = new Integer[0];
      assertThat(intersection(self, new Integer[]{4, 5, 6}, Integer[].class))
        .isEqualTo(self);
    }

    @Test
    public void returnsOtherWhenOtherIsEmpty() {
      final var other = new Integer[0];
      assertThat(intersection(new Integer[]{1, 2, 3}, other, Integer[].class))
        .isEqualTo(other);
    }

    @Test
    public void computesIntersection() {
      assertThat(
        intersection(
          new Integer[]{1, 2, 3},
          new Integer[]{2, 3, 4},
          Integer[].class
        )
      )
        .containsExactly(2, 3);
    }
  }

  public static final class IsNullOrEmptyTests {
    @SuppressWarnings("ConstantValue")
    @Test
    public void returnsTrueWhenNull() {
      assertThat(isNullOrEmpty(null)).isTrue();
    }

    @Test
    public void returnsTrueWhenEmpty() {
      assertThat(isNullOrEmpty(new int[]{})).isTrue();
    }

    @Test
    public void returnFalseWhenPopulated() {
      assertThat(isNullOrEmpty(new int[]{1, 2, 3})).isFalse();
    }
  }

  public static final class UnionTests {
    @Test
    public void returnsNullWhenEitherArrayIsNull() {
      assertThat(union(null, new Integer[0])).isNull();
      assertThat(union(new Integer[0], null)).isNull();
    }

    @Test
    public void returnsSelfWhenOtherIsNull() {
      final Integer[] self = {1, 2, 3};
      assertThat(union(self, new Integer[0])).isEqualTo(self);
    }

    @Test
    public void returnsOtherWhenSelfIsNull() {
      final Integer[] other = {4, 5, 6};
      assertThat(union(new Integer[0], other)).isEqualTo(other);
    }

    @Test
    public void computesUnion() {
      assertThat(union(new Integer[]{1, 2, 3}, new Integer[]{4, 5, 6}))
        .containsExactly(1, 2, 3, 4, 5, 6);
      assertThat(union(new Integer[]{1, 2, 4}, new Integer[]{6, 5, 3}))
        .containsExactly(1, 2, 3, 4, 5, 6);
    }
  }
}