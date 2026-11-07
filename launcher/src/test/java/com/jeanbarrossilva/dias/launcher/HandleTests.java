package com.jeanbarrossilva.dias.launcher;

import android.content.Context;

import androidx.test.espresso.intent.Intents;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.jeanbarrossilva.dias.launcher.HandleAssert.assertThat;
import static com.jeanbarrossilva.dias.launcher.Handle.SAMPLE;

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