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

package com.jeanbarrossilva.dias.launcher.system.persistence;

import androidx.room.Database;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RoomDatabase;

import static java.lang.Math.toIntExact;
import static java.util.Arrays.stream;

@Database(entities = {Pin.class}, exportSchema = false, version = 1)
public abstract class PinDatabase extends RoomDatabase {
  @androidx.room.Dao
  public abstract static class Dao {
    Dao() {}

    @Query("SELECT * FROM pins ORDER BY id")
    public abstract Pin[] selectAll();

    @Query("SELECT id FROM pins ORDER BY id")
    public abstract String[] selectAllIDs();

    @Query("SELECT COUNT(*) FROM pins")
    public abstract int count();

    @Query("""
      SELECT * FROM pins
      WHERE NOT EXISTS (
        SELECT 1 FROM pins
        WHERE id IN (:ids)
      )""")
    public abstract String[] diff(final String... ids);

    @Query("""
      SELECT CASE
        WHEN EXISTS (SELECT * FROM pins WHERE id = :id)
        THEN CAST(1 AS BIT)
        ELSE CAST(0 AS BIT)
      END""")
    public abstract boolean isPinned(final String id);

    public final boolean insert(final String id) {
      final var pin = new Pin(id);
      return insert(pin).length > 0;
    }

    public final int insert(final String... ids) {
      final var pins = stream(ids).map(Pin::new).toArray(Pin[]::new);
      return toIntExact(stream(insert(pins)).filter(n -> n >= 0).count());
    }

    public final boolean delete(final String id) {
      return delete(new String[]{id}) == 1;
    }

    @Query("DELETE FROM pins WHERE id IN (:ids)")
    public abstract int delete(final String... ids);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract long[] insert(final Pin... pins);
  }

  public abstract Dao getDao();
}