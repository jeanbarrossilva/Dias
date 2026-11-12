package com.jeanbarrossilva.dias.app.util;

import androidx.annotation.NonNull;
import androidx.collection.MutableObjectIntMap;

/// Extensions for [MutableObjectIntMap]s.
public final class MutableObjectIntMaps {
  /// Allows for instantiating a [MutableObjectIntMap] declaratively.
  public static final class Builder<Key> {
    private MutableObjectIntMap<Key> result;
    private boolean isDirty;

    /// Instantiates a builder of a [MutableObjectIntMap].
    ///
    /// @param initialCapacity Amount of key–value pairs expected to be put into
    ///   the map to be built. Surpassing this quantity will result in growth of
    ///   the map.
    public Builder(final int initialCapacity) {
      this.result = new MutableObjectIntMap<>(initialCapacity);
      this.isDirty = false;
    }

    /// Associates a value to a key in the map being built.
    ///
    /// The order in which key–value pairs are put isn't guaranteed to match
    /// their index in the map.
    ///
    /// @return This builder.
    @NonNull
    public Builder<Key> put(final Key key, final int value) {
      copyIfDirty();
      result.put(key, value);
      return this;
    }

    /// Instantiates a map containing the key–value pairs put into it.
    ///
    /// @return The built map.
    @NonNull
    public MutableObjectIntMap<Key> build() {
      copyIfDirty();
      isDirty = true;
      return result;
    }

    private void copyIfDirty() {
      if (!isDirty)
        return;
      result = copy(result);
      isDirty = false;
    }
  }

  private MutableObjectIntMaps() {}

  /// Instantiates a map containing each of the given map's key–value pairs.
  ///
  /// @param self Map whose entries will be copied to the map to be returned.
  /// @return A copy of the given map, or `null` if the given map is `null`.
  @SuppressWarnings({"KotlinInternalInJava", "unchecked"})
  public static <Key> MutableObjectIntMap<Key> copy(
    final MutableObjectIntMap<Key> self
  ) {
    if (self == null)
      return null;
    final var result = new MutableObjectIntMap<Key>(self._capacity);
    for (int index = 0; index < self.count(); index++) {
      final Key key = (Key) self.keys[index];
      result.put(key, self.get(key), index);
    }
    return result;
  }
}