package com.jeanbarrossilva.dias.core;

/** Extensions for objects of any type. */
public final class Objects {
  private Objects() {}

  /**
   * Returns the object of the given two that isn't {@code null}.
   *
   * @param self Object to be returned in case the other is {@code null}.
   * @param other Object to be returned in case {@code self} is {@code null}.
   * @return The object that isn't {@code null}, or {@code null} if both are
   *   {@code null}.
   * @param <Result> One of both objects.
   */
  public static <Result> Result either(final Result self, final Result other) {
    return other == null ? self : other;
  }
}