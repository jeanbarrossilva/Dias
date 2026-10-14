package com.jeanbarrossilva.dias.app.home;

import android.graphics.RectF;
import android.view.View;

/** Extensions for {@link View}s. */
final class Views {
  private Views() {}

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