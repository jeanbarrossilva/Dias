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

package com.jeanbarrossilva.dias.launcher.local;

import static java.util.Arrays.stream;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import com.jeanbarrossilva.dias.launcher.Handle;
import com.jeanbarrossilva.dias.launcher.Launcher;
import com.jeanbarrossilva.dias.launcher.SearchSession;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/// Launcher that runs on any JVM and doesn't depend on features exclusive to an
/// Android device.
///
/// Handles that are pinned in an instance of this class are remembered for as
/// long the instance exists, and only by that instance itself; new instances
/// will have no pins by default.
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public final class LocalLauncher extends Launcher {
  private final HashSet<Handle> pins;

  public LocalLauncher(@NonNull final Context context) {
    super(context, Handle.createSamples());
    this.pins = new HashSet<>();
  }

  @NonNull
  @Override
  public Handle[] findPins() {
    return pins.toArray(Handle[]::new);
  }

  @Override
  public boolean isPinned(@Nullable final String id) {
    return findPin(id) != null;
  }

  @Override
  public void clear() {
    pins.clear();
  }

  @Override
  protected int countPins() {
    return pins.size();
  }

  @Override
  protected String[] getPinIDs() {
    return pins.stream().map(Handle::getID).toArray(String[]::new);
  }

  @Override
  @SuppressWarnings("DataFlowIssue")
  protected boolean onPinning(@NonNull final Pinning pinning) {
    final List<Handle> toggledPins =
      stream(getPins(pinning.indices())).filter(Objects::nonNull).toList();
    if (pinning.arePinned())
      return pins.addAll(toggledPins);
    else {
      var didUnpin = false;
      for (final Handle pin: toggledPins)
        if (pins.remove(pin) && !didUnpin)
          didUnpin = true;
      return didUnpin;
    }
  }

  @Override
  protected SearchSession createSearchSession(@Nullable final Context context) {
    return new LocalSearchSession();
  }

  @Override
  protected void onClose() {}

  @Nullable
  private Handle findPin(@Nullable final String id) {
    return id == null ? null : pins
      .stream()
      .filter(handle -> handle.getID().equals(id))
      .findFirst()
      .orElse(null);
  }
}