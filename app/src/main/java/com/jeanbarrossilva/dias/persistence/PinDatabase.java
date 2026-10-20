package com.jeanbarrossilva.dias.persistence;

import androidx.room.Database;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RoomDatabase;

import static java.util.Arrays.stream;

@Database(entities = {Pin.class}, exportSchema = false, version = 1)
public abstract class PinDatabase extends RoomDatabase {
  @androidx.room.Dao
  public abstract static class Dao {
    Dao() {}

    @Query("SELECT * FROM pins")
    public abstract Pin[] selectAll();

    @Query("SELECT COUNT(*) FROM pins")
    public abstract int count();

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
      return insert(pins).length;
    }

    @Query("DELETE FROM pins WHERE id IN (:ids)")
    public abstract int delete(final String... ids);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract long[] insert(final Pin... pins);
  }

  public abstract Dao getDao();
}