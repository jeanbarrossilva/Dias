package com.jeanbarrossilva.dias;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ListAssert;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static org.assertj.core.api.InstanceOfAssertFactories.BOOLEAN;
import static org.assertj.core.api.InstanceOfAssertFactories.list;

public final class LauncherAssert
  extends AbstractAssert<LauncherAssert, Launcher> {
  private LauncherAssert(@NotNull final Launcher launcher) {
    super(launcher, LauncherAssert.class);
  }

  @NotNull
  public ListAssert<Launcher.Handle> pins() {
    return describedAs("pins").extracting(
      launcher -> Arrays.asList(launcher.findPins()),
      Assertions.as(list(Launcher.Handle.class))
    );
  }

  @NotNull
  @SuppressWarnings("resource")
  public LauncherAssert hasPinned(final int index) {
    describedAs("is " + actual().getHandles().get(index) + " pinned")
      .extracting(launcher -> launcher.isPinned(index), Assertions.as(BOOLEAN))
      .isEqualTo(true);
    return this;
  }

  @NotNull
  @SuppressWarnings("resource")
  public LauncherAssert hasUnpinned(final int index) {
    describedAs("is " + actual().getHandles().get(index) + " unpinned")
      .extracting(launcher -> launcher.isPinned(index), Assertions.as(BOOLEAN))
      .isEqualTo(false);
    return this;
  }

  @NotNull
  public static LauncherAssert assertThat(@NotNull final Launcher launcher) {
    return new LauncherAssert(launcher);
  }
}