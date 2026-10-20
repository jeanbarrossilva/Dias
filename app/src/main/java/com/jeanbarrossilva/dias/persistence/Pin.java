package com.jeanbarrossilva.dias.persistence;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.jeanbarrossilva.dias.Launcher;

@Entity(tableName = "pins")
public final class Pin {
  @NonNull
  @PrimaryKey
  public final String id;

  Pin(final String id) {
    this.id = id;
  }

  public Launcher.Handle toHandle(final Launcher launcher) {
    return new Launcher.Handle(id, launcher.label(id));
  }
}