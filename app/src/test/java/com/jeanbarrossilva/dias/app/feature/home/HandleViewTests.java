package com.jeanbarrossilva.dias.app.feature.home;

import android.content.Context;
import android.content.res.Resources;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@RunWith(RobolectricTestRunner.class)
public class HandleViewTests {
  @Test
  public void throwsOnInflation() {
    final Context context = getApplicationContext();
    assertThatThrownBy(() -> new com.jeanbarrossilva.dias.app.ui.HandleView(context, null))
      .isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> new com.jeanbarrossilva.dias.app.ui.HandleView(context, null, Resources.ID_NULL))
      .isInstanceOf(UnsupportedOperationException.class);
  }
}