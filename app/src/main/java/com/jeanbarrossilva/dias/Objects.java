package com.jeanbarrossilva.dias;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/** Extensions for objects of any type. */
public final class Objects {
  private Objects() {}

  /**
   * Attempts to cast an object to a type.
   *
   * @param <R> Type to which the object will be attempted to be cast.
   * @param returnClass Class of {@code R}.
   * @param self Object to cast to {@code R}.
   * @return The object as an {@code R}, or {@code null} in case the cast fails.
   */
  @Nullable
  @SuppressWarnings("unchecked")
  public static <R> R as(
    @NonNull final Class<R> returnClass,
    @Nullable final Object self
  ) {
    if (self == null)
      return null;
    else if (returnClass.isInstance(self.getClass())
          || returnClass.isAssignableFrom(self.getClass()))
      return (R) self;
    else
      return null;
  }
}