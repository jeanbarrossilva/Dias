package com.jeanbarrossilva.dias.launcher;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;

import com.google.common.collect.ContiguousSet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.hash;

/**
 * Holds information about a user-visible, installed application.
 */
@SuppressWarnings("ClassCanBeRecord")
public final class Handle implements Comparable<Handle> {
  /** Handles for demonstration purposes. */
  @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
  public static final Handle[] SAMPLES =
    ContiguousSet.closedOpen(0, 13)
      .stream()
      .map(
        index -> new Handle(
          /* packageName = */  "com.jeanbarrossilva.dias",
          /* activityName = */ "com.jeanbarrossilva.dias.Activity"
          + (index + 1),
          /* label = */        "App " + (index + 1)
        )
      )
      .toArray(Handle[]::new);

  /** One of the {@link #SAMPLES}. */
  @VisibleForTesting
  static final Handle SAMPLE = SAMPLES[0];

  /**
   * Identifier of this handle. Consists of this handle's associated
   * activity's flattened name.
   */
  @NonNull
  public final String id;

  /**
   * Localized text considered to be the name of the application.
   */
  @NonNull
  public final String label;

  @VisibleForTesting
  Handle(
    final String packageName,
    final String activityName,
    final String label
  ) throws NullPointerException {
    this(/* id = */ packageName + '/' + activityName, label);
  }

  @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
  public Handle(final String id, final String label) {
    this.id = id;
    this.label = label;
  }

  /**
   * Instantiates an intent for querying handles.
   *
   * @see #queryAll(PackageManager, Intent)
   */
  @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
  public static Intent createQueryIntent() {
    return new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
  }

  @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
  public static Map<String, Handle> queryAll(
    final PackageManager packageManager,
    final Intent intent
  ) {
    final List<ResolveInfo> resolveInfos =
      packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL);
    final var result =
      new HashMap<String, Handle>(/* initialCapacity = */ resolveInfos.size());
    queryAll(packageManager, resolveInfos, result);
    return result;
  }

  @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
  public static void queryAll(
    final PackageManager packageManager,
    final Intent intent,
    final Map<String, Handle> destination
  ) {
    queryAll(
      packageManager,
      packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL),
      destination
    );
  }

  @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
  public static void queryAll(
    final PackageManager packageManager,
    final List<ResolveInfo> resolveInfos,
    final Map<String, Handle> destination
  ) {
    for (final ResolveInfo resolveInfo: resolveInfos) {
      final Handle value = parse(packageManager, resolveInfo.activityInfo);
      destination.put(value.id, value);
    }
  }

  private static Handle parse(
    final PackageManager packageManager,
    final ActivityInfo activityInfo
  ) {
    if (activityInfo == null)
      return null;
    final String packageName = activityInfo.packageName;
    if (packageName == null)
      return null;
    final String activityName = activityInfo.name;
    if (activityName == null)
      return null;
    final String label = activityInfo.loadLabel(packageManager).toString();
    return new Handle(packageName, activityName, label);
  }

  @Override
  public boolean equals(@Nullable final Object obj) {
    return obj instanceof Handle typedObj
      && id.equals(typedObj.id)
      && label.equals(typedObj.label);
  }

  @Override
  public int hashCode() {
    return hash(id, label);
  }

  @NonNull
  @Override
  public String toString() {
    return label + " (" + id + ')';
  }

  @Override
  public int compareTo(final Handle o) {
    return label.compareTo(o.label);
  }

  /** Instantiates the name of the activity associated to this handle. */
  @VisibleForTesting
  ComponentName name() {
    return ComponentName.unflattenFromString(id);
  }

  /**
   * Starts the activity associated to this handle.
   *
   * @param context Context from which to start the activity.
   * @throws ActivityNotFoundException If this handle is associated to an
   *                                   activity nonexistent in the given context.
   */
  public void launch(@NonNull final Context context)
    throws ActivityNotFoundException {
    final Intent intent = new Intent()
      .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      .setComponent(ComponentName.unflattenFromString(id));
    context.startActivity(intent);
  }
}