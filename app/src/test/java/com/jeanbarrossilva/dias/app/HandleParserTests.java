package com.jeanbarrossilva.dias.app;

import android.content.ComponentName;
import android.content.Context;

import com.jeanbarrossilva.dias.Launcher;
import com.jeanbarrossilva.dias.testing.handle.ActivityInfoBuilder;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import kotlin.reflect.KProperty1;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.jeanbarrossilva.dias.testing.handle.HandleParsingAssertion.assertThatHandleParsing;

@RunWith(RobolectricTestRunner.class)
public final class HandleParserTests {
  @Test
  public void throwsOnMissingName() {
    final Context context = getApplicationContext();
    assertThatHandleParsing(context, ActivityInfoBuilder::withoutName)
      .throwsWithUnparsableActivityInfo()
      .properties()
      .extracting(KProperty1::getName)
      .containsExactly("name");
  }

  @Test
  public void throwsOnMissingLabel() {
    final Context context = getApplicationContext();
    assertThatHandleParsing(
      context,
      activityInfoBuilder -> activityInfoBuilder
        .withoutLabelRes()
        .withoutNonLocalizedLabel()
    )
      .throwsWithUnparsableActivityInfo()
      .properties()
      .extracting(KProperty1::getName)
      .containsExactlyInAnyOrder("labelRes", "nonLocalizedLabel");
  }

  @Test
  public void throwsOnMissingPackageName() {
    final Context context = getApplicationContext();
    assertThatHandleParsing(context, ActivityInfoBuilder::withoutPackageName)
      .throwsWithUnparsableActivityInfo()
      .properties()
      .extracting(KProperty1::getName)
      .containsExactly("packageName");
  }

  @Ignore("unimplemented parsing of activity class")
  @Test
  public void throwsOnNonexistentActivity() {
    final Context context = getApplicationContext();
    assertThatHandleParsing(
      context,
      activityInfoBuilder -> activityInfoBuilder
        .withName("ImprobableNameForAnExistingActivity")
    )
      .throwsWithNonexistentActivity();
  }

  @Test
  public void parses() {
    final Context context = getApplicationContext();
    assertThatHandleParsing(context, activityInfoBuilder -> {})
      .succeeds()
      .isEqualTo(
        new Launcher.Handle(
          new ComponentName(
            ActivityInfoBuilder.defaultPackageName,
            ActivityInfoBuilder.DefaultActivity.class.getName()
          ),
          ActivityInfoBuilder.defaultLabel
        )
      );
  }
}