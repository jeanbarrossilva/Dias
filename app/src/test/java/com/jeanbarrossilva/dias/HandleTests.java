package com.jeanbarrossilva.dias;

import android.content.Context;

import androidx.test.espresso.intent.Intents;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.jeanbarrossilva.dias.Handles.SAMPLE;
import static com.jeanbarrossilva.dias.HandleAssert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class HandleTests {
  @Before
  public void setUp() {
    Intents.init();
  }

  @Test
  public void launches() {
    final Context context = getApplicationContext();
    SAMPLE.launch(context);
    assertThat(SAMPLE).launched();
  }

  @After
  public void tearDown() {
    Intents.release();
  }
}