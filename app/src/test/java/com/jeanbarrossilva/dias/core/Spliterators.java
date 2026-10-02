package com.jeanbarrossilva.dias.core;

import java.util.ArrayList;
import java.util.Spliterator;

import static java.util.Collections.unmodifiableList;
import static org.apache.commons.collections4.list.LazyList.lazyList;

/** Extensions for {@link Spliterator}s. */
public class Spliterators {
  private Spliterators() {}

  /**
   * Converts a spliterator into an iterable.
   *
   * @param self Spliterator to be converted into a list.
   * @param <Element> An element of the spliterator.
   */
  @SuppressWarnings("unchecked")
  public static <Element> Iterable<Element> toIterable(
    final Spliterator<? extends Element> self
  ) {
    if (!self.hasCharacteristics(Spliterator.SIZED)) {
      final var backingList = new ArrayList<Element>();
      return lazyList(backingList, () -> {
        final Element[] elements = (Element[]) new Object[1];
        self.tryAdvance(element -> elements[0] = element);
        return elements[0];
      });
    }
    final long exactSize = self.getExactSizeIfKnown();
    final var backingList =
      new ArrayList<Element>(/* initialCapacity = */ clamped(exactSize));
    self.forEachRemaining(backingList::add);
    return unmodifiableList(backingList);
  }

  private static int clamped(final long n) {
    return Math.clamp(n, Integer.MIN_VALUE, Integer.MAX_VALUE);
  }
}