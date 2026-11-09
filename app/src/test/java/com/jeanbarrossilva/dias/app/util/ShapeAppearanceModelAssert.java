package com.jeanbarrossilva.dias.app.util;

import android.graphics.RectF;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.shape.CornerSize;
import com.google.android.material.shape.ShapeAppearanceModel;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.FloatAssert;

public final class ShapeAppearanceModelAssert
  extends AbstractAssert<ShapeAppearanceModelAssert, ShapeAppearanceModel> {
  private static final RectF BOUNDS = new RectF(
    /* left = */   0,
    /* top = */    0,
    /* right = */  Float.MAX_VALUE,
    /* bottom = */ Float.MAX_VALUE
  );;

  private ShapeAppearanceModelAssert(final ShapeAppearanceModel actual) {
    super(actual, ShapeAppearanceModelAssert.class);
  }

  /// Asserts that the size of every corner of the model is exactly equal to the
  /// given one.
  ///
  /// @param cornerSize Size expected to be that of each of the model's corners.
  @NonNull
  public ShapeAppearanceModelAssert hasAllCornerSizes(final float cornerSize)
    throws AssertionError {
    final var asserts = new FloatAssert[]{
      getTopLeftCornerSize(),
      getTopRightCornerSize(),
      getBottomLeftCornerSize(),
      getBottomRightCornerSize()
    };
    if (cornerSize == 0)
      for (final FloatAssert cornerSizeAssert: asserts)
        cornerSizeAssert.isZero();
    else
      for (final FloatAssert cornerSizeAssert: asserts)
        cornerSizeAssert.isEqualTo(cornerSize);
    return this;
  }

  /// Returns an asserter for the shape's top left corner's size.
  ///
  /// @throws AssertionError If the model is `null`.
  @NonNull
  public FloatAssert getTopLeftCornerSize() throws AssertionError {
    return createCornerSizeAssert(
                          isNotNull().actual().getTopLeftCornerSize(),
      /* description = */ "topLeft"
    );
  }

  /// Returns an asserter for the shape's top right corner's size.
  ///
  /// @throws AssertionError If the model is `null`.
  @NonNull
  public FloatAssert getTopRightCornerSize() throws AssertionError {
    return createCornerSizeAssert(
                          isNotNull().actual().getTopRightCornerSize(),
      /* description = */ "topRight"
    );
  }

  /// Returns an asserter for the shape's bottom left corner's size.
  ///
  /// @throws AssertionError If the model is `null`.
  @NonNull
  public FloatAssert getBottomLeftCornerSize() throws AssertionError {
    return createCornerSizeAssert(
                          isNotNull().actual().getBottomLeftCornerSize(),
      /* description = */ "bottomLeft"
    );
  }

  /// Returns an asserter for the shape's bottom right corner's size.
  ///
  /// @throws AssertionError If the model is `null`.
  @NonNull
  public FloatAssert getBottomRightCornerSize() throws AssertionError {
    return createCornerSizeAssert(
                          isNotNull().actual().getBottomRightCornerSize(),
      /* description = */ "bottomRight"
    );
  }

  /// Instantiates a [ShapeAppearanceModelAssert].
  ///
  /// @param actual Model for which the asserter is.
  /// @return The instantiated asserter.
  @NonNull
  public static ShapeAppearanceModelAssert assertThat(
    @Nullable final ShapeAppearanceModel actual
  ) {
    return new ShapeAppearanceModelAssert(actual).describedAs(
      actual == null
        ? ShapeAppearanceModel.class.getSimpleName()
        : actual.toString()
    );
  }

  private FloatAssert createCornerSizeAssert(
    final CornerSize cornerSize,
    final String description
  ) {
    return new FloatAssert(cornerSize.getCornerSize(BOUNDS))
      .describedAs(descriptionText() + '.' + description);
  }
}