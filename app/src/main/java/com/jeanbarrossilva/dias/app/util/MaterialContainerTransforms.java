package com.jeanbarrossilva.dias.app.util;

import static com.jeanbarrossilva.dias.app.util.GradientDrawables.toShapeAppearanceModel;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.transition.MaterialContainerTransform;

/// Extensions for [MaterialContainerTransform]s.
public final class MaterialContainerTransforms {
  private MaterialContainerTransforms() {}

  /// Configures a transition between the given start and end views, defining
  /// the end view being the only target.
  ///
  /// @param self Transform to configure.
  /// @param startView View to animate from.
  /// @param endView View to animate to.
  public static void setUpTransition(
    final MaterialContainerTransform self,
    final View startView,
    final View endView
  ) {
    if (self == null)
      return;
    final ShapeAppearanceModel startShape = createShape(startView);
    self.setStartView(startView);
    if (startShape != null)
      self.setEndShapeAppearanceModel(startShape);
    if (endView == null) {
      final View oldEndView = self.getEndView();
      if (oldEndView != null) {
        self.removeTarget(oldEndView);
        self.setEndShapeAppearanceModel(null);
      }
    }
    else {
      final ShapeAppearanceModel endShape = createShape(endView);
      self.setEndView(endView);
      if (endShape != null)
        self.setEndShapeAppearanceModel(endShape);
      self.addTarget(endView);
    }
  }

  private static ShapeAppearanceModel createShape(final View view) {
    final Drawable background = view == null ? null : view.getBackground();
    return background != null
      && background.getCurrent() instanceof GradientDrawable currentBackground
      ? toShapeAppearanceModel(currentBackground)
      : null;
  }
}