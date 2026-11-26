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

package com.jeanbarrossilva.dias.launcher.system;

import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import com.jeanbarrossilva.dias.launcher.Handle;
import com.jeanbarrossilva.dias.launcher.Launcher;
import com.jeanbarrossilva.dias.launcher.SearchSession;
import com.jeanbarrossilva.dias.launcher.local.LocalLauncher;
import com.jeanbarrossilva.dias.launcher.system.persistence.PinDatabase;

import org.agrona.collections.IntHashSet;

/// Launcher that relies on features of an Android device. This is the
/// implementation of the [Launcher] class to be used in production.
///
/// Handles get persisted when pinned, and the pins will be recovered whenever
/// this class is instantiated with the same search index name as that of the
/// launcher in which those handles were pinned. E.g.:
///
/// ```java
/// final var setupStrategy = Launcher.SetupStrategy.sample();
/// final var searchIndexName = "search-index"
/// final var launcher1 = new Launcher.system(context, pinDatabase, searchIndexName, setupStrategy);
/// final Handle pin = launcher.getHandles().getFirst();
/// launcher1.pin(pin.getID());
/// launcher1.close();
/// final var launcher2 = new Launcher.system(context, pinDatabase, searchIndexName, setupStrategy);
/// assertThat(launcher2).hasPinned(pin.getID());
/// launcher2.close();
/// ```
///
/// @implNote This might seem unnecessary from a practical,
///   non-object-oriented-puristic perspective. Why not simply make this the
///   single, final class, as opposed to splitting implementations into a system
///   and a local one?
///
///   There'd be a point to be made for Robolectric, because, in v4.16.1, it
///   doesn't implement AndroidX AppSearch yet; but we're testing search
///   sessions in an emulator in Dias v1.0.0. Since that's already sorted out,
///   the main reason is UI previews: some require a launcher (which is…
///   arguable?), and previews don't implement these Android-device-specific
///   features we depend on. To them, a [LocalLauncher] should be provided
///   instead.
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public final class SystemLauncher extends Launcher {
  private final PinDatabase pinDatabase;
  private final String searchIndexName;

  /// Instantiates a system launcher.
  ///
  /// @param context Context through which information about installed
  ///   applications are queried and parsed into handles.
  /// @param pinDatabase Database for persisting pinned handles.
  /// @param handles Unique handles to all applications visible to the user,
  ///   sorted by label.
  /// @param searchIndexName Name of the index in which info about each handle,
  ///   tailored for a session, is persisted.
  public SystemLauncher(
    @Nullable final Context context,
    @NonNull final PinDatabase pinDatabase,
    @NonNull final String searchIndexName,
    @NonNull final Handle[] handles
  ) throws NullPointerException {
    super(context, handles);
    this.pinDatabase = requireNonNull(pinDatabase, "pinDatabase");
    this.searchIndexName = requireNonNull(searchIndexName, "searchIndexName");
  }

  @NonNull
  @Override
  public Handle[] findPins() {
    return stream(getPinDao().selectAll())
      .map(pin -> pin.toHandle(this))
      .toArray(Handle[]::new);
  }

  @Override
  public boolean isPinned(@Nullable String id) {
    return getPinDao().isPinned(id);
  }

  @Override
  public void clear() {
    pinDatabase.clearAllTables();
  }

  @Override
  protected int countPins() {
    return getPinDao().count();
  }

  @Override
  protected String[] getPinIDs() {
    return getPinDao().selectAllIDs();
  }

  @Override
  protected boolean onPinning(@NonNull Pinning pinning) {
    final IntHashSet.IntIterator handleIndexIterator =
      pinning.indices().iterator();
    final var ids = new String[pinning.indices().size()];
    for (int index = 0; index < ids.length; index++)
      ids[index] = getBackingHandles()[handleIndexIterator.nextValue()].getID();
    return (pinning.arePinned()
      ? getPinDao().insert(ids)
      : getPinDao().delete(ids)) > 0;
  }

  @Override
  protected SearchSession createSearchSession(@Nullable Context context) {
    return new SystemSearchSession(context, searchIndexName);
  }

  @Override
  protected void onClose() {
    pinDatabase.close();
  }

  private PinDatabase.Dao getPinDao() {
    return pinDatabase.getDao();
  }
}