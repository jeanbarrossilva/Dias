package com.jeanbarrossilva.dias.app.util;

import static android.graphics.drawable.GradientDrawable.RECTANGLE;
import static com.jeanbarrossilva.dias.app.util.GradientDrawables.toShapeAppearanceModel;
import static com.jeanbarrossilva.dias.app.util.ShapeAppearanceModelAssert.assertThat;

import android.graphics.drawable.GradientDrawable;


import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(Enclosed.class)
public final class GradientDrawablesTests {
  @RunWith(RobolectricTestRunner.class)
  public static final class ToShapeAppearanceModelTests {
    @Test
    public void returnsNullWhenSelfIsNull() {
      Assertions.assertThat(toShapeAppearanceModel(null)).isNull();
    }

    @Test
    public void convertsSelfWithZeroesCornerSizes() {
      assertThat(toShapeAppearanceModel(createSelf())).hasAllCornerSizes(0);
    }

    @Test
    public void convertsSelfWithSingleCornerRadius() {
      final var self = createSelf();
      self.setCornerRadius(2);
      assertThat(toShapeAppearanceModel(self)).hasAllCornerSizes(2);
    }

    @Test
    public void convertsSelfWithVariousCornerRadii() {
      final var self = createSelf();
      final var cornerRadii = new float[]{2, 4, 16, 256};
      self.setCornerRadii(cornerRadii);
      final ShapeAppearanceModelAssert modelAssert =
        assertThat(toShapeAppearanceModel(self));
      modelAssert.getTopLeftCornerSize().isEqualTo(cornerRadii[0]);
      modelAssert.getTopRightCornerSize().isEqualTo(cornerRadii[1]);
      modelAssert.getBottomLeftCornerSize().isEqualTo(cornerRadii[2]);
      modelAssert.getBottomRightCornerSize().isEqualTo(cornerRadii[3]);
    }

    private GradientDrawable createSelf() {
      final var result = new GradientDrawable();
      result.setShape(RECTANGLE);
      return result;
    }
  }
}