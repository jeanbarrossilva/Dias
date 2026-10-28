package com.jeanbarrossilva.dias.app;

import android.graphics.Insets;
import android.graphics.RectF;
import android.view.View;

/** Extensions for {@link View}s. */
public final class Views {
  private Views() {}

  /**
   * Moves the view away from the given insets.
   *
   * @param self The view to be pushed away from the insets.
   * @param insets Insets that may be obstructed by the view, away from which
   *   the view may be pushed.
   */
  public static void push(final View self, final Insets insets) {
    if (self == null || insets == null)
      return;
    self.setTranslationX(insets.left - insets.right);
    self.setTranslationY(insets.top - insets.bottom);
  }

  /**
   * Updates the given rectangle to the position and the size of the view
   * relative to the view's parent.
   *
   * @param self View whose parent-relative bounds will be set to the rectangle.
   * @param out Rectangle to be updated to represent the view's, relative to the
   *   view's parent.
   */
  public static void getBoundsInParent(final View self, final RectF out) {
    if (self == null || out == null)
      return;
    final float x = self.getX();
    final float y = self.getY();
    out.left = x;
    out.top = y;
    out.right = x + self.getWidth();
    out.bottom = y + self.getHeight();
  }
}