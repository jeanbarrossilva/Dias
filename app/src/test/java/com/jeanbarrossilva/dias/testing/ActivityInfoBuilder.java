package com.jeanbarrossilva.dias.testing;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.jeanbarrossilva.dias.R;

public final class ActivityInfoBuilder {
  @NonNull public static final String defaultLabel = "Dias";

  @StringRes private int labelRes;
  @Nullable private String name;
  @Nullable private String nonLocalizedLabel;
  @Nullable private String packageName;

  public static final class DefaultActivity extends Activity {}

  public ActivityInfoBuilder() {
    labelRes = R.string.app_name;
    name = DefaultActivity.class.getName();
    nonLocalizedLabel = defaultLabel;
    packageName = "com.jeanbarrossilva.dias";
  }

  @NonNull
  public ActivityInfoBuilder withoutLabelRes() {
    labelRes = Resources.ID_NULL;
    return this;
  }

  @NonNull
  @SuppressWarnings("UnusedReturnValue")
  public ActivityInfoBuilder withoutName() {
    return withName(null);
  }

  @NonNull
  public ActivityInfoBuilder withName(@Nullable final String name) {
    this.name = name;
    return this;
  }

  @NonNull
  @SuppressWarnings("UnusedReturnValue")
  public ActivityInfoBuilder withoutNonLocalizedLabel() {
    nonLocalizedLabel = null;
    return this;
  }

  @NonNull
  @SuppressWarnings("UnusedReturnValue")
  public ActivityInfoBuilder withoutPackageName() {
    this.packageName = null;
    return this;
  }

  @NonNull
  ActivityInfo build() {
    final ActivityInfo activityInfo = new ActivityInfo();
    activityInfo.labelRes = labelRes;
    activityInfo.name = name;
    activityInfo.nonLocalizedLabel = nonLocalizedLabel;
    activityInfo.packageName = packageName;
    return activityInfo;
  }
}