package com.jeanbarrossilva.dias.app.util;

import android.view.ViewGroup;

import java.lang.reflect.InvocationTargetException;

/// Extensions for [ViewGroup.LayoutParams].
public final class LayoutParams {
  private LayoutParams() {}

  /// Instantiates layout parameters for a view, based on the given layout
  /// parameters. This method differs from the copy constructor in that it
  /// returns parameters with the same margins as those of the given parameters
  /// in case these given parameters are also [ViewGroup.MarginLayoutParams].
  ///
  /// @param self Parameters from which ones with margins will be instantiated.
  /// @return The parameters with margins, derived from the given parameters; or
  ///   `null` in case the given parameters were `null`.
  public static ViewGroup.MarginLayoutParams cloneWithMargins(
    final ViewGroup.LayoutParams self
  ) {
    if (self == null)
      return null;
    if (self instanceof final ViewGroup.MarginLayoutParams castSelf) {
      try {
        final ViewGroup.MarginLayoutParams result = castSelf
          .getClass()
          .getDeclaredConstructor(ViewGroup.LayoutParams.class)
          .newInstance(castSelf);
        assert result.leftMargin == castSelf.leftMargin;
        assert result.topMargin == castSelf.topMargin;
        assert result.rightMargin == castSelf.rightMargin;
        assert result.bottomMargin == castSelf.bottomMargin;
        return result;
      } catch (final IllegalAccessException
                   | InstantiationException
                   | InvocationTargetException
                   | NoSuchMethodException exception) {
        final var result = new ViewGroup.MarginLayoutParams(self);
        result.leftMargin = castSelf.leftMargin;
        result.topMargin = castSelf.topMargin;
        result.rightMargin = castSelf.rightMargin;
        result.bottomMargin = castSelf.bottomMargin;
        return result;
      }
    } else
      return new ViewGroup.MarginLayoutParams(self);
  }
}