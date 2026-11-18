package com.jeanbarrossilva.dias.app.util;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.graphics.Insets;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.InvocationTargetException;

/// Extensions for [ViewGroup.LayoutParams].
public final class LayoutParameters {
  private LayoutParameters() {}

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

  /**
   * Sets the margins of the layout parameters according to the given insets.
   * This allows for moving a view out of the insets' way via its margins
   * (that is, after calling this function on the layout parameters, by setting
   * them as those of that view).
   *
   * @param self Layout parameters whose margins will be changed.
   * @param insets Insets that'll inform the margins of the given layout
   *   parameters.
   * @see View#setLayoutParams(ViewGroup.LayoutParams)
   */
  public static void setMargins(
    final ViewGroup.MarginLayoutParams self,
    final Insets insets
  ) {
    if (insets == null)
      return;
    self.leftMargin = insets.left;
    self.topMargin = insets.top;
    self.rightMargin = insets.right;
    self.bottomMargin = insets.bottom;
  }

  /// Converts the given layout parameters into a readable string, containing
  /// their specified width and height.
  ///
  /// @param self Layout parameters to convert into a string.
  /// @return The string representation of the layout parameters; `"null"` if
  ///   they are `null`.
  public static String toString(final ViewGroup.LayoutParams self) {
    if (self == null)
      return "null";
    final var prefix = self.getClass().getSimpleName()
      + "[width="
      + toString(self.width)
      + ", height="
      + toString(self.height);
    return prefix
      + (self instanceof final ViewGroup.MarginLayoutParams marginedSelf
        ? ", leftMargin="
            + marginedSelf.leftMargin
            + ", topMargin="
            + marginedSelf.topMargin
            + ", rightMargin="
            + marginedSelf.rightMargin
            + ", bottomMargin="
            + marginedSelf.bottomMargin
        : "")
      + ']';
  }

  private static String toString(final int size) {
    return switch (size) {
      case MATCH_PARENT -> "MATCH_PARENT";
      case WRAP_CONTENT -> "WRAP_CONTENT";
      default -> String.valueOf(size);
    };
  }
}