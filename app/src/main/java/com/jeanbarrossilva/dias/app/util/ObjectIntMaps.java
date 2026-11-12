package com.jeanbarrossilva.dias.app.util;

import androidx.annotation.NonNull;
import androidx.collection.MutableObjectIntMap;
import androidx.collection.ObjectIntMap;

import org.agrona.collections.Object2IntHashMap;

import java.util.Map;

import kotlin.Unit;

/// Extensions for [ObjectIntMap]s.
public final class ObjectIntMaps {
  /// Allows for instantiating a [ObjectIntMap] declaratively.
  public static final class Builder<Key> {
    private MutableObjectIntMap<Key> result;
    private boolean isDirty;

    /// Instantiates a builder of a [ObjectIntMap].
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
    public ObjectIntMap<Key> build() {
      copyIfDirty();
      isDirty = true;
      return result;
    }

    private void copyIfDirty() {
      if (!isDirty)
        return;
      result = (MutableObjectIntMap<Key>) cloneOf(result);
      isDirty = false;
    }
  }

  private ObjectIntMaps() {}

  /// Instantiates a map containing each of the given map's key–value pairs.
  ///
  /// @param self Map whose entries will be copied to the map to be returned.
  /// @return A clone of the given map, or `null` if the given map is `null`.
  public static <Key> ObjectIntMap<Key> cloneOf(final ObjectIntMap<Key> self) {
    if (self == null)
      return null;
    final var result = new MutableObjectIntMap<Key>(self.getCapacity());
    if (self.isNotEmpty())
      self.forEachKey(key -> {
        result.put(key, self.get(key));
        return Unit.INSTANCE;
      });
    return result;
  }

  /// Converts the given object–int map into an instance that implements the
  /// [Map] interface.
  ///
  /// @param self Object–int map to be converted into a [Map].
  public static <Key> Object2IntHashMap<Key> toMap(
    final ObjectIntMap<Key> self
  ) {
    final var result = new Object2IntHashMap<Key>(self.getCapacity());
    self.forEachKey(key -> {
      result.put(key, self.get(key));
      return Unit.INSTANCE;
    });
    return result;
  }
}