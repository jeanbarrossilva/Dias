package com.jeanbarrossilva.dias.core;

import com.google.common.collect.ContiguousSet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.util.List;

import static com.jeanbarrossilva.dias.core.OneTimeCopyOnWriteArrayListAssert.assertThat;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.INTEGER;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  OneTimeCopyOnWriteArrayListTests.ArrayListTests.class,
  OneTimeCopyOnWriteArrayListTests.CustomTests.class
})
public class OneTimeCopyOnWriteArrayListTests {
  private static final ContiguousSet<Integer> sampleBackedListIndices =
    ContiguousSet.closedOpen(0, 12);

  public static final class CustomTests {
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

  public static final class ArrayListTests {
    @Test
    public void adds() {
      final var standaloneList = new OneTimeCopyOnWriteArrayList<Integer>(1);
      standaloneList.add(0);
      assertThat(standaloneList).containsExactly(0);

      final Object[] backingArray = sampleBackingArray();
      final var backedList = new OneTimeCopyOnWriteArrayList<>(backingArray);
      backedList.add(0);
      assertThat(backedList).last().asInstanceOf(INTEGER).isZero();
    }

    @Test
    public void addingEmptyCollectionDoesNotCoW() {
      final Object[] backingArray = sampleBackingArray();
      final var backedList = new OneTimeCopyOnWriteArrayList<>(backingArray);
      backedList.addAll(IntArrayLists.of());
      assertThat(backedList, backingArray).didNotCoW();
    }

    @Test
    public void addsPopulatedCollection() {
      final var standaloneList = new OneTimeCopyOnWriteArrayList<Integer>(2);
      standaloneList.addAll(IntArrayLists.of(0, 1));
      assertThat(standaloneList).containsExactly(0, 1);

      final Object[] backingArray = sampleBackingArray();
      final var backedList = new OneTimeCopyOnWriteArrayList<>(backingArray);
      backedList.addAll(IntArrayLists.of(0, 1));
      assertThat(backedList, backingArray).didCoW();
    }

    @Test
    public void addingPopulatedCollectionCoWs() {
      final Object[] backingArray = sampleBackingArray();
      final var backedList = new OneTimeCopyOnWriteArrayList<>(backingArray);
      backedList.addAll(IntArrayLists.of(0, 1));
      assertThat(backedList, backingArray).didCoW();
    }

    @Test
    public void clearingWhenBackedByPopulatedListCoWs() {
      final Object[] backingArray = sampleBackingArray();
      final var backedList = new OneTimeCopyOnWriteArrayList<>(backingArray);
      backedList.clear();
      assertThat(backedList, backingArray).didCoW();
    }

    @SuppressWarnings("EqualsWithItself")
    @Test
    public void clones() {
      final var standaloneList = new OneTimeCopyOnWriteArrayList<>(0);
      assertThat(standaloneList)
        .asInstanceOf(type(OneTimeCopyOnWriteArrayList.class))
        .extracting(OneTimeCopyOnWriteArrayList::clone)
        .isEqualTo(standaloneList);

      final Object[] backingArray = sampleBackingArray();
      final var backedList = new OneTimeCopyOnWriteArrayList<>(backingArray);
      assertThat(backedList)
        .asInstanceOf(type(OneTimeCopyOnWriteArrayList.class))
        .extracting(OneTimeCopyOnWriteArrayList::clone)
        .isEqualTo(backedList);
    }

    @Test
    public void contains() {
      final var standaloneList = new OneTimeCopyOnWriteArrayList<>(0);
      assertThat(standaloneList).doesNotContain(0);

      final Object[] backingArray = sampleBackingArray();
      final var backedList = new OneTimeCopyOnWriteArrayList<>(backingArray);
      assertThat(backedList).containsExactly(backingArray);
    }

    @Test
    public void compares() {
      final var standaloneList = new OneTimeCopyOnWriteArrayList<>(0);
      assertThat(standaloneList)
        .isNotEqualTo(new Object())
        .isEqualTo(List.of());

      final var emptyBackingArray = new Object[0];
      final var emptyBackedList =
        new OneTimeCopyOnWriteArrayList<>(emptyBackingArray);
      assertThat(emptyBackedList, emptyBackingArray)
        .isNotEqualTo(new Object())
        .isEqualTo(List.of());

      final var populatedBackingArray = sampleBackingArray();
      final var populatedBackedList =
        new OneTimeCopyOnWriteArrayList<>(populatedBackingArray);
      assertThat(populatedBackedList, populatedBackingArray)
        .isNotEqualTo(new Object())
        .isNotEqualTo(List.of())
        .isEqualTo(asList(populatedBackingArray));
    }
  }

  private static Object[] sampleBackingArray() {
    return sampleBackedListIndices
      .stream()
      .map(index -> new Object())
      .toArray();
  }
}