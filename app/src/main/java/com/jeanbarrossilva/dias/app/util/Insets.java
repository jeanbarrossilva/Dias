package com.jeanbarrossilva.dias.app.util;

import static java.util.Objects.requireNonNull;

/// Extensions for [android.graphics.Insets].
public final class Insets {
  private Insets() {}

  /// Computes the height of the given insets.
  ///
  /// @param self Insets whose height will be computed.
  /// @throws NullPointerException If the insets are `null`.
  public static int height(final android.graphics.Insets self)
    throws NullPointerException {
    return requireNonNull(self, "self").bottom - self.top;
  }
}