package com.jeanbarrossilva.dias.core;

import org.assertj.core.api.AbstractListAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ObjectAssert;

import java.util.function.BiConsumer;

public class OneTimeCopyOnWriteArrayListAssert<Element>
  extends AbstractListAssert<OneTimeCopyOnWriteArrayListAssert<Element>,
                             OneTimeCopyOnWriteArrayList<Element>,
                             Element,
                             ObjectAssert<Element>> {
  private final Object[] backingArray;

  private OneTimeCopyOnWriteArrayListAssert(
    final OneTimeCopyOnWriteArrayList<Element> actual,
    final Element[] backingArray
  ) {
    super(actual, OneTimeCopyOnWriteArrayListAssert.class);
    this.backingArray = backingArray;
  }

  public OneTimeCopyOnWriteArrayListAssert<Element> didNotCoW()
    throws AssertionError {
    writeToBackingArray((previousElement, elementIndex) -> {
      final Object currentElement = backingArray[elementIndex];
      try {
        element(elementIndex).isEqualTo(currentElement);
      } catch (final AssertionError error) {
        isEmpty();
      }
    });
    return this;
  }

  public OneTimeCopyOnWriteArrayListAssert<Element> didCoW()
    throws AssertionError {
    writeToBackingArray((previousElement, elementIndex) -> {
      try {
        element(elementIndex).isEqualTo(previousElement);
      } catch (final AssertionError error) {
        isEmpty();
      }
    });
    return this;
  }

  @Override
  protected ObjectAssert<Element> toAssert(
    final Element value,
    final String description
  ) {
    return Assertions.assertThat(value);
  }

  @Override
  protected OneTimeCopyOnWriteArrayListAssert<Element> newAbstractIterableAssert(
    final Iterable<? extends Element> iterable
  ) {
    return this;
  }

  private void writeToBackingArray(
    final BiConsumer<Object, Integer> modification
  ) {
    final int elementIndex = 0;
    final Object previousElement = backingArray[0];
    backingArray[0] = new Object();
    try { modification.accept(previousElement, elementIndex); }
    finally { backingArray[0] = previousElement; }
  }

  public static <Element> OneTimeCopyOnWriteArrayListAssert<Element> assertThat(
    final Element[] backingArray
  ) {
    final var actual = new OneTimeCopyOnWriteArrayList<>(backingArray);
    return assertThat(actual, backingArray);
  }

  public static <Element> OneTimeCopyOnWriteArrayListAssert<Element> assertThat(
    final OneTimeCopyOnWriteArrayList<Element> actual,
    final Element[] backingArray
  ) {
    return new OneTimeCopyOnWriteArrayListAssert<>(actual, backingArray);
  }
}