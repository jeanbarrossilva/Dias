package com.jeanbarrossilva.dias;

import android.content.Context;
import android.content.pm.PackageManager;

import com.jeanbarrossilva.dias.testing.ActivityInfoBuilder;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import kotlin.reflect.KProperty1;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.jeanbarrossilva.dias.testing.HandleParsingAssertion.assertThatHandleParsing;

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
  public void parses() throws PackageManager.NameNotFoundException {
    final Context context = getApplicationContext();
    final PackageManager packageManager = context.getPackageManager();
    final String packageName = context.getPackageName();
    assertThatHandleParsing(context, activityInfoBuilder -> {})
      .succeeds()
      .isEqualTo(
        new Handle(
          packageManager.getPackageUid(packageName, 0),
          ActivityInfoBuilder.defaultLabel
       // ActivityInfoBuilder.DefaultActivity.class
        )
      );
  }
}