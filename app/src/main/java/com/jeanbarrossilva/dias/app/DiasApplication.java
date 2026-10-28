package com.jeanbarrossilva.dias.app;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;

import com.jeanbarrossilva.dias.launcher.Launcher;
import com.jeanbarrossilva.dias.launcher.persistence.PinDatabase;

import static androidx.room.Room.inMemoryDatabaseBuilder;

public class DiasApplication extends Application {
  public Launcher launcher;

  private Lifecycle.State lifecycleState = Lifecycle.State.INITIALIZED;

  @Override
  public void onCreate() {
    super.onCreate();
    lifecycleState = Lifecycle.State.CREATED;
    launcher = new Launcher(
      this,
      inMemoryDatabaseBuilder(this, PinDatabase.class)
        .allowMainThreadQueries()
        .build()
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