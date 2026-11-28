package com.jeanbarrossilva.dias.launcher.util;

import java.util.Iterator;

/** Extensions for {@link Iterable}s. */
public class Iterables {
  private Iterables() {}

  /**
   * Joins the string representation of every element to a single string.
   * <p>
   * This method differs from {@link String#join(CharSequence, Iterable)} in
   * that it allows that a different separator between the second to last and
   * the last element be defined, which may incur in more natural phrasing.
   *
   * @param self Iterable with the elements to be joined.
   * @param delimiter Separator for elements before the last one.
   * @param conjunction Separator for the second to last and last element.
   */
  public static String joinToString(
    final Iterable<?> self,
    final String delimiter,
    final String conjunction
  ) {
    if (self == null)
      return null;
    final Iterator<?> iterator = self.iterator();
    if (!iterator.hasNext())
      return "";
    final StringBuilder jointBuilder = new StringBuilder();
    Boolean hasNext = null;
    while (hasNext == null || hasNext) {
      final Object next = iterator.next();
      if (hasNext != null)
        jointBuilder.append(iterator.hasNext() ? delimiter : conjunction);
      hasNext = iterator.hasNext();
      jointBuilder.append(next);
    }
    return jointBuilder.toString();
  }
}