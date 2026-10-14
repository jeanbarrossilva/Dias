package com.jeanbarrossilva.dias;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.ListAssert;
import org.jetbrains.annotations.NotNull;

import static com.jeanbarrossilva.dias.core.Asserts.assertThatCall;
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
    assertThatReturnValueOfHasHandles().isTrue();
    return this;
  }

  @NotNull
  public LauncherAssert hasPins() {
    assertThatReturnValueOfHasPins().isTrue();
    return this;
  }

  @NotNull
  public LauncherAssert doesNotHavePins() {
    assertThatReturnValueOfHasPins().isFalse();
    return this;
  }

  @NotNull
  public ListAssert<Launcher.Handle> pins() {
    return assertThatCall(actual(), "findPins", array(Launcher.Handle[].class))
      .asInstanceOf(listFromArray(Launcher.Handle[].class));
  }

  @NotNull
  @SuppressWarnings("resource")
  public LauncherAssert hasPinned(final int index) {
    describedAs("is " + actual().getHandles().get(index) + " pinned")
      .extracting(launcher -> launcher.isPinned(index), BOOLEAN)
      .isEqualTo(true);
    return this;
  }

  @NotNull
  @SuppressWarnings("resource")
  public LauncherAssert hasUnpinned(final int index) {
    describedAs("is " + actual().getHandles().get(index) + " unpinned")
      .extracting(launcher -> launcher.isPinned(index), BOOLEAN)
      .isEqualTo(false);
    return this;
  }

  @NotNull
  public static LauncherAssert assertThat(@NotNull final Launcher launcher) {
    return new LauncherAssert(launcher);
  }

  private AbstractBooleanAssert<?> assertThatReturnValueOfHasHandles() {
    return assertThatCall(actual(), "hasHandles", BOOLEAN);
  }

  private AbstractBooleanAssert<?> assertThatReturnValueOfHasPins() {
    return assertThatCall(actual(), "hasPins", BOOLEAN);
  }
}