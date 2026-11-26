/*
 * Copyright © 2026 Jean Silva
 *
 * Dias is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 2 of the License, or (at your option) any later
 * version.
 *
 * Dias is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Dias. If not, see https://www.gnu.org/licenses.
 */

package com.jeanbarrossilva.dias.launcher;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Parcel;
import android.os.Parcelable;

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
public final class Handle implements Comparable<Handle>, Parcelable {
  /**
   * A handle for demonstration purposes.
   *
   * @see #createSamples()
   */
  @VisibleForTesting
  static final Handle SAMPLE = createSamples()[0];

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

  /// Instantiator of handles from parcels.
  public static final Creator<Handle> CREATOR = new Creator<>() {
    @Override
    public Handle createFromParcel(Parcel in) {
      return new Handle(in);
    }

    @Override
    public Handle[] newArray(int size) {
      return new Handle[size];
    }
  };

  /// Instantiates a handle based on a parcelized one.
  ///
  /// @param in The parcelized handle.
  public Handle(final Parcel in) {
    this(/* id = */ in.readString(), /* label = */ in.readString());
  }

  /// Instantiates a handle.
  ///
  /// @param id Identifier of this handle. Consists of this handle's associated
  ///   activity's flattened name.
  /// @param label Localized text considered to be the name of the application.
  @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
  public Handle(final String id, final String label) {
    this.id = id;
    this.label = label;
  }

  @VisibleForTesting
  Handle(
    final String packageName,
    final String activityName,
    final String label
  ) throws NullPointerException {
    this(/* id = */ packageName + '/' + activityName, label);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(@NonNull Parcel parcel, int i) {
    parcel.writeString(id);
    parcel.writeString(label);
  }

  /** Instantiates handles for demonstration purposes. */
  public static Handle[] createSamples() {
    return ContiguousSet.closedOpen(0, 13)
      .stream()
      .map(
        index -> new Handle(
          /* packageName = */ "com.jeanbarrossilva.dias",
          /* activityName = */ "com.jeanbarrossilva.dias.Activity"
            + (index + 1),
          /* label = */ "App " + (index + 1)
        )
      )
      .toArray(Handle[]::new);
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

  private static void queryAll(
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

  /**
   * Instantiates the name of the activity associated to this handle.
   *
   * @see #SAMPLE
   */
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