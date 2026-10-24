package com.jeanbarrossilva.dias.launcher.persistence;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.jeanbarrossilva.dias.launcher.Handle;
import com.jeanbarrossilva.dias.launcher.Launcher;

@Entity(tableName = "pins")
public final class Pin {
  @NonNull
  @PrimaryKey
  public final String id;

  Pin(final String id) {
    this.id = id;
  }

  public Handle toHandle(final Launcher launcher) {
    return new Handle(id, launcher.label(id));
  }
}