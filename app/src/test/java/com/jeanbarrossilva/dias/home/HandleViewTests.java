package com.jeanbarrossilva.dias.home;

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
    assertThatThrownBy(() -> new HandleView(context, null))
      .isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> new HandleView(context, null, Resources.ID_NULL))
      .isInstanceOf(UnsupportedOperationException.class);
  }
}