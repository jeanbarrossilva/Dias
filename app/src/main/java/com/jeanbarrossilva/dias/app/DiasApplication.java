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

package com.jeanbarrossilva.dias.app;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;

import com.jeanbarrossilva.dias.launcher.Launcher;
import com.jeanbarrossilva.dias.launcher.system.persistence.PinDatabase;

import static androidx.room.Room.inMemoryDatabaseBuilder;

public class DiasApplication extends Application {
  public Launcher launcher;

  private Lifecycle.State lifecycleState = Lifecycle.State.INITIALIZED;

  @Override
  public void onCreate() {
    super.onCreate();
    lifecycleState = Lifecycle.State.CREATED;
    launcher = Launcher.system(
      this,
      inMemoryDatabaseBuilder(this, PinDatabase.class)
        .allowMainThreadQueries()
        .build(),
      /* searchIndexName = */ "launcher-search-index",
      Launcher.SetupStrategy.fetch()
    );
  }

  @Override
  public void onTerminate() {
    super.onTerminate();
    lifecycleState = Lifecycle.State.DESTROYED;
    launcher.close();
  }

  @NonNull
  public Lifecycle.State getLifecycleState() {
    return lifecycleState;
  }
}