package com.jeanbarrossilva.dias;

import androidx.annotation.NonNull;

import org.assertj.core.api.AbstractObjectAssert;

import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.assertNoUnverifiedIntents;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;

public class HandleAssert extends AbstractObjectAssert<HandleAssert, Launcher.Handle> {
  private HandleAssert(@NonNull final Launcher.Handle handle) {
    super(handle, HandleAssert.class);
  }

  @NonNull
  public HandleAssert launched() throws AssertionError {
    intended(hasComponent(actual().name));
    assertNoUnverifiedIntents();
    return this;
  }

  public static HandleAssert assertThat(@NonNull final Launcher.Handle handle) {
    return new HandleAssert(handle);
  }
}
