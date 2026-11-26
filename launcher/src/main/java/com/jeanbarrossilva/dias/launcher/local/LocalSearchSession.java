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

package com.jeanbarrossilva.dias.launcher.local;

import static java.util.Arrays.stream;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import com.jeanbarrossilva.dias.launcher.Handle;
import com.jeanbarrossilva.dias.launcher.SearchSession;
import com.jeanbarrossilva.dias.launcher.installation.InstallationDiff;

import java.util.List;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public final class LocalSearchSession extends SearchSession {
  /// Instantiator of local sessions from parcels.
  public static final Parcelable.Creator<LocalSearchSession> CREATOR =
    new Parcelable.Creator<>() {
      @Override
      public LocalSearchSession createFromParcel(@NonNull final Parcel source) {
        return new LocalSearchSession(source);
      }

      @Override
      public LocalSearchSession[] newArray(final int size) {
        return new LocalSearchSession[size];
      }
    };

  /// Instantiates a local session.
  public LocalSearchSession() {
    super();
  }

  /// Instantiates a local session from a parceled one.
  ///
  /// @param in The parceled session.
  /// @throws NullPointerException If the parceled session is `null`.
  public LocalSearchSession(@NonNull final Parcel in)
    throws NullPointerException {
    super(in);
  }

  @Override
  protected void onSync(
    @Nullable final Context context,
    @NonNull final Handle[] handles
  ) {}

  @Override
  protected void onSync(
    @Nullable final Context context,
    @NonNull final InstallationDiff diff
  ) {}

  @NonNull
  @Override
  protected List<Handle> onSearch(
    @Nullable final Context context,
    @NonNull final String query
  ) {
    final Handle[] handles = getHandles();
    return handles == null ? List.of() : stream(handles)
      .filter(handle -> normalize(handle.getLabel()).contains(query))
      .toList();
  }

  @Override
  protected void onEnding(@Nullable final Context context) {}
}