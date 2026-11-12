package com.jeanbarrossilva.dias.app.util;

import static com.jeanbarrossilva.dias.app.util.ObjectIntMaps.cloneOf;
import static com.jeanbarrossilva.dias.app.util.ObjectIntMaps.toMap;
import static org.assertj.core.api.Assertions.assertThat;

import androidx.collection.ObjectIntMap;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.Map;

@RunWith(Enclosed.class)
public final class ObjectIntMapsTests {
  private static final ObjectIntMaps.Builder<String> BUILDER =
    new ObjectIntMaps.Builder<String>(/* initialCapacity = */ 3)
      .put("a", 0)
      .put("b", 1)
      .put("c", 2);
  private static final ObjectIntMap<String> SELF = BUILDER.build();

  @SuppressWarnings("unchecked")
  private static final Map.Entry<String, Integer>[] ENTRIES =
    toMap(SELF).entrySet().toArray(Map.Entry[]::new);

  public static final class BuilderTests {
    @Test
    public void builds() {
      assertThat(toMap(SELF)).containsExactly(ENTRIES);
    }

    @Test
    public void buildingTwiceConsecutivelyInstantiatesNewMaps() {
      assertThat(SELF).isNotSameAs(BUILDER.build());
    }
  }

  public static final class CloneTests {
    @Test
    public void returnsNullWhenCloningNullMap() {
      assertThat(cloneOf(null)).isNull();
    }

    @Test
    public void clones() {
      final ObjectIntMap<String> clone = cloneOf(SELF);
      assertThat(clone).isNotSameAs(SELF);
      assertThat(toMap(clone)).containsExactly(ENTRIES);
    }
  }
}