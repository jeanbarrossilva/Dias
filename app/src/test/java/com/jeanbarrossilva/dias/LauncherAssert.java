package com.jeanbarrossilva.dias;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.ListAssert;
import org.jetbrains.annotations.NotNull;
import org.springframework.test.util.MethodAssert;

import java.lang.reflect.InvocationTargetException;

import static com.jeanbarrossilva.dias.core.InstanceOfAssertFactories.listFromArray;
import static org.assertj.core.api.InstanceOfAssertFactories.array;
import static org.assertj.core.api.InstanceOfAssertFactories.BOOLEAN;

public final class LauncherAssert
  extends AbstractAssert<LauncherAssert, Launcher> {
  private LauncherAssert(@NotNull final Launcher launcher) {
    super(launcher, LauncherAssert.class);
  }

  @NotNull
  public LauncherAssert hasHandles() {
    assertThatHasHandles().isTrue();
    return this;
  }

  @NotNull
  public LauncherAssert hasPins() {
    assertThatHasPins().isTrue();
    return this;
  }

  @NotNull
  public LauncherAssert doesNotHavePins() {
    assertThatHasPins().isFalse();
    return this;
  }

  @NotNull
  public ListAssert<Launcher.Handle> pins() {
    try {
      return MethodAssert.assertThat(Launcher.class, "findPins")
                         .invokeAsync(actual())
                         .asInstanceOf(listFromArray(Launcher.Handle[].class));
    } catch (final NoSuchMethodException cause) {
      throw new RuntimeException(cause);
    }
  }

  @NotNull
  public LauncherAssert hasPinned(final String id) {
    assertThatIsPinned(id).isTrue();
    return this;
  }

  @NotNull
  public LauncherAssert hasUnpinned(final String id) {
    assertThatIsPinned(id).isFalse();
    return this;
  }

  @NotNull
  public static LauncherAssert assertThat(@NotNull final Launcher launcher) {
    return new LauncherAssert(launcher);
  }

  private AbstractBooleanAssert<?> assertThatHasHandles() {
    try {
      return MethodAssert.assertThat(Launcher.class, "hasHandles")
                         .invoke(actual())
                         .asInstanceOf(BOOLEAN);
    } catch (final IllegalAccessException
                 | InvocationTargetException
                 | NoSuchMethodException cause) {
      throw new RuntimeException(cause);
    }
  }

  private AbstractBooleanAssert<?> assertThatHasPins() {
    try {
      return MethodAssert.assertThat(Launcher.class, "hasPins")
                         .invokeAsync(actual())
                         .asInstanceOf(BOOLEAN);
    } catch (final NoSuchMethodException cause) {
      throw new RuntimeException(cause);
    }
  }

  private AbstractBooleanAssert<?> assertThatIsPinned(final String id) {
    try {
      return MethodAssert.assertThat(Launcher.class, "isPinned", String.class)
                         .invokeAsync(actual(), id)
                         .asInstanceOf(BOOLEAN);
    } catch (final NoSuchMethodException cause) {
      throw new RuntimeException(cause);
    }
  }
}