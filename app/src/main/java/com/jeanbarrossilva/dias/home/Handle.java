package com.jeanbarrossilva.dias.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;
import java.util.stream.Stream;

public class Handle implements Comparable<Handle> {
  @NonNull public final CharSequence label;

  @NonNull private final Class<? extends Activity> activityClass;

  public Handle(
    @NonNull final CharSequence label,
    @NonNull final Class<? extends Activity> activityClass
  ) {
    this.label = label;
    this.activityClass = activityClass;
  }

  @Override
  public int compareTo(Handle o) {
    return CharSequence.compare(label, o.label);
  }

  public void launch(@NonNull final Context context) {
    final Intent intent = new Intent(context, activityClass);
    context.startActivity(intent);
  }

  @NonNull
  public static Stream<Handle> queryAll(@NonNull final Context context) {
    final PackageManager packageManager = context.getPackageManager();
    final Intent intent = new Intent(Intent.ACTION_MAIN, null);
    intent.addCategory(Intent.CATEGORY_LAUNCHER);
    return packageManager
      .queryIntentActivities(intent, PackageManager.MATCH_ALL)
      .stream()
      .filter(Objects::nonNull)
      .map(resolveInfo -> derive(context, resolveInfo.activityInfo))
      .filter(Objects::nonNull)
      .sorted();
  }

  @Nullable
  private static Handle derive(
    @NonNull final Context context,
    @NonNull final ActivityInfo activityInfo
  ) {
    final Class<? extends Activity> activityClass;
    final ClassLoader activityClassLoader =
      findClassLoader(context, activityInfo.packageName);
    if (activityClassLoader == null)
      return null;
    try {
      activityClass = Class
        .forName(
          activityInfo.name,
          /* initialize = */ true,
          activityClassLoader
        )
        .asSubclass(Activity.class);
    } catch (final ClassNotFoundException exception) {
      return null;
    }
    final PackageManager packageManager = context.getPackageManager();
    final CharSequence label = activityInfo.loadLabel(packageManager);
    return new Handle(label, activityClass);
  }

  private static ClassLoader findClassLoader(
    @NonNull final Context context,
    @NonNull final String packageName
  ) {
    try {
      return context
        .createPackageContext(
          packageName,
          Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE
        )
        .getClassLoader();
    } catch (final PackageManager.NameNotFoundException exception) {
      return null;
    }
  }
}
