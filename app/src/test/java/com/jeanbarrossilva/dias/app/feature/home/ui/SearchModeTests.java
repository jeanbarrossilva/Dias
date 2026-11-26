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

package com.jeanbarrossilva.dias.app.feature.home.ui;

import static android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import android.os.Bundle;
import android.os.Parcel;

import androidx.annotation.NonNull;

import com.jeanbarrossilva.dias.app.feature.home.Mode;
import com.jeanbarrossilva.dias.launcher.Launcher;
import com.jeanbarrossilva.dias.launcher.SearchSession;

import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(Enclosed.class)
public final class SearchModeTests {
  @RunWith(RobolectricTestRunner.class)
  public static final class ParcelizationTests {
    private static final String KEY = "mode";

    @Test
    public void parcelizes() {
      final Launcher launcher = createLauncher();
      final var mode = new Mode.Search(launcher);
      final var bundle = new Bundle();
      bundle.putParcelable(KEY, mode);
      assertThat(bundle.getParcelable(KEY, Mode.Search.class))
        .usingRecursiveComparison(
          RecursiveComparisonConfiguration.builder()
            .withComparedFields("session", "queryState", "results")
            .withStrictTypeChecking(true)
            .build()
        )
        .isEqualTo(mode);
      mode.close();
      launcher.close();
    }

    @Test
    public void endsSessionWhenWritingReturnValue() {
      final Launcher launcher = createLauncher();
      final var mode = new Mode.Search(launcher);
      final Parcel parcel = Parcel.obtain();
      mode.writeToParcel(parcel, PARCELABLE_WRITE_RETURN_VALUE);
      assertThatThrownBy(() -> mode.setQuery("🪲"))
        .isInstanceOf(SearchSession.EndedException.class);
      parcel.recycle();
      mode.close();
      launcher.close();
    }
  }

  @NonNull
  private static Launcher createLauncher() {
    return Launcher.local(getApplicationContext());
  }
}