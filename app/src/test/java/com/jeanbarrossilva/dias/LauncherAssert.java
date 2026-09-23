package com.jeanbarrossilva.dias;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.ObjectAssert;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static androidx.test.espresso.intent.Intents.assertNoUnverifiedIntents;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static org.assertj.core.api.InstanceOfAssertFactories.BOOLEAN;
import static org.assertj.core.api.InstanceOfAssertFactories.list;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

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
  @SuppressWarnings({"resource", "UnusedReturnValue"})
  public LauncherAssert hasLaunched(final int index)
    throws AssertionError {
    final Launcher.Handle handle = actual().getHandles().get(index);
    intended(hasComponent(handle.name));
    assertNoUnverifiedIntents();
    return this;
  }

  @NotNull
  public ObjectAssert<Launcher.Handle> get(final int index) {
    return handles().element(index).asInstanceOf(type(Launcher.Handle.class));
  }

  @NotNull
  public ListAssert<Launcher.Handle> handles() {
    return describedAs("handles").extracting(
      Launcher::getHandles,
      Assertions.as(list(Launcher.Handle.class))
    );
  }

  @NotNull
  public static LauncherAssert assertThat(@NotNull final Launcher launcher) {
    return new LauncherAssert(launcher);
  }
}