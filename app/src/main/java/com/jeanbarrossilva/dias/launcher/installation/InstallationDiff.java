/*
 * Copyright © 2026 Jean Silva
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see https://www.gnu.org/licenses.
 */

package com.jeanbarrossilva.dias.launcher.installation;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;

import com.jeanbarrossilva.dias.launcher.Handle;

import java.util.Arrays;

import static com.jeanbarrossilva.dias.core.Arrays.union;
import static java.util.Arrays.binarySearch;
import static java.util.Objects.hash;

/**
 * Change to some package's installation relative to the handles passed into an
 * {@link InstallationChangeReceiver}, denoting that the package has been
 * modified or one of its applications has been installed or uninstalled; which
 * of the three took place is given by the {@link #kind}.
 */
public final class InstallationDiff {
  /**
   * Handles to all applications of the changed package.
   * <p>
   * This array is guaranteed to be sorted and contain distinct handles by
   * default.
   *
   * @implNote Given another array of handles <i>A</i> and that this diff is of
   *   an installation, when merging this diff into <i>A</i>, it is reasonable
   *   for the caller to assume that none of the handles held by this field are
   *   in <i>A</i> (i.e., {@code handles} \ <i>A</i> = {@code handles}). This
   *   stems from our assumption that this class was instantiated because of an
   *   actual installation, notified by the system.
   * @see #merge(Handle[])
   */
  public final Handle[] handles;

  /**
   * Indices of the {@link InstallationChangeReceiver}'s handle array at which
   * this diff's handles are; or, in case this is an addition or a replacement,
   * at which they should be inserted.
   * <p>
   * This array is guaranteed to be sorted and contain distinct indices by
   * default.
   */
  public final int[] indices;

  /**
   * Denotes whether a diff pertains to an <i>installation</i>, an
   * <i>uninstallation</i> or a <i>replacement</i>.
   */
  public final Kind kind;

  /** Category of an installation-related change occurred to a given package. */
  public enum Kind {
    /** One or more applications have been installed. */
    INSTALLATION,

    /** One or more applications have been uninstalled. */
    UNINSTALLATION,

    /** The package or one or more of its applications were modified. */
    REPLACEMENT;

    /**
     * Converts an intent into a kind of installation diff.
     *
     * @param intent Intent to be parsed.
     * @return The kind equivalent to the given intent, or {@code null} if the
     *   intent…
     *
     * <ul>
     *   <li>…is {@code null};</li>
     *   <li>has a {@code null} action;</li>
     *   <li>
     *     is of a package removal which will be followed by an addition intent
     *     pertaining to that same package; or
     *   </li>
     *   <li>
     *     isn't of a package addition preceded by an intent for removing that
     *     same package, or isn't a removal intent.
     *   </li>
     * </ul>
     */
    static Kind parse(final Intent intent) {
      if (intent == null)
        return null;
      final String action = intent.getAction();
      if (action == null)
        return null;
      return switch (action) {
        case Intent.ACTION_PACKAGE_ADDED ->
          intent.hasExtra(Intent.EXTRA_REPLACING)
            ? REPLACEMENT
            : INSTALLATION;
        case Intent.ACTION_PACKAGE_FULLY_REMOVED ->
          UNINSTALLATION;
        case Intent.ACTION_PACKAGE_REPLACED ->
          REPLACEMENT;
        case Intent.ACTION_PACKAGE_REMOVED ->
          intent.hasExtra(Intent.EXTRA_REPLACING) ? null : UNINSTALLATION;
        default ->
          null;
      };
    }
  }

  @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
  InstallationDiff(
    final Handle[] handles,
    final int[] indices,
    final Kind kind
  ) {
    this.handles = handles;
    this.indices = indices;
    this.kind = kind;
  }

  @Override
  public boolean equals(final Object o) {
    return o instanceof InstallationDiff castO
      && Arrays.equals(handles, castO.handles)
      && Arrays.equals(indices, castO.indices)
      && kind == castO.kind;
  }

  @Override
  public int hashCode() {
    return hash(Arrays.hashCode(handles), Arrays.hashCode(indices), kind);
  }

  @NonNull
  @Override
  public String toString() {
    return "InstallationDiff{handles="
      + Arrays.toString(handles)
      + ", indices="
      + Arrays.toString(indices)
      + ", kind="
      + kind
      + '}';
  }

  /**
   * Applies the changes contained in this diff to the given array. They'll be
   * applied in place in case they fit into it, and {@code destination} will be
   * the array returned by this method; otherwise, a new array is allocated and
   * returned, with the given one remaining unchanged.
   *
   * @param destination Array to which the changes represented by this diff will
   *   be merged.
   *   <p>
   *   It is assumed that this array is provided by a launcher and, thus, is
   *   sorted, with none of its elements being {@code null}. The behavior of
   *   this method may be undefined in scenarios in which these conditions
   *   aren't satisfied.
   * @return An array with the changes applied.
   */
  @NonNull
  @RestrictTo(RestrictTo.Scope.LIBRARY)
  public Handle[] merge(@NonNull final Handle[] destination) {
    Handle[] result = destination;
    switch (kind) {
      case INSTALLATION ->
        result = union(destination, handles, Handle[].class);
      case REPLACEMENT -> {
        for (int index = 0; index < indices.length; index++)
          result[indices[index]] = handles[index];
      }
      case UNINSTALLATION -> {
        result = new Handle[destination.length - handles.length];
        for (int index = 0, j = 0; index < destination.length; index++)
          if (binarySearch(indices, index) < 0)
            result[j++] = destination[index];
      }
    }
    return result;
  }
}