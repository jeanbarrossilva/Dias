package com.jeanbarrossilva.dias;

import org.junit.Test;

import java.util.ArrayList;

import static com.jeanbarrossilva.dias.ArrayLists.reserveExactCapacity;
import static org.assertj.core.api.Assertions.assertThat;

public final class ArrayListsTests {
  @Test
  public void reservesExactCapacity() {
    final int capacity = 8;
    final ArrayList<Integer> self = new ArrayList<>(capacity);
    for (int element = 1; element <= capacity; element++)
      self.add(element);
    reserveExactCapacity(self, 2);
    assertThat(self).containsExactly(1, 2);
  }
}