package com.jeanbarrossilva.dias.app.util;

import static com.jeanbarrossilva.dias.app.util.Objects.either;

import android.graphics.drawable.GradientDrawable;

import com.google.android.material.shape.ShapeAppearanceModel;

/// Extensions for [GradientDrawable]s.
public final class GradientDrawables {
  private GradientDrawables() {}

  /// Converts the given drawable into a [ShapeAppearanceModel].
  ///
  /// @param self Drawable to be converted.
  public static ShapeAppearanceModel toShapeAppearanceModel(
    final GradientDrawable self
  ) {
    if (self == null)
      return null;
    final float[] cornerRadii = either(
      self.getCornerRadii(),
      new float[]{
        self.getCornerRadius(),
        self.getCornerRadius(),
        self.getCornerRadius(),
        self.getCornerRadius()
      }
    );
    return ShapeAppearanceModel.builder()
      .setTopLeftCornerSize(cornerRadii[0])
      .setTopRightCornerSize(cornerRadii[1])
      .setBottomLeftCornerSize(cornerRadii[2])
      .setBottomRightCornerSize(cornerRadii[3])
      .build();
  }
}