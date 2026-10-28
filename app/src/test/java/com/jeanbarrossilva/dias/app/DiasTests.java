package com.jeanbarrossilva.dias.app;

import android.app.Activity;
import android.app.Application;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

import static com.jeanbarrossilva.dias.app.Dias.requireLauncher;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public final class DiasTests {
  @RunWith(RobolectricTestRunner.class)
  public static final class RequireLauncherTests {
    @SuppressWarnings("resource")
    @Test
    public void throwsWhenAppIsNotOfDias() {
      final var nonDiasApp = new Application();
      final var activity = mock(Activity.class);
      when(activity.getApplication()).thenReturn(nonDiasApp);
      assertThatThrownBy(() -> requireLauncher(activity))
        .isInstanceOf(ClassCastException.class);
      clearInvocations(activity);
    }

    @SuppressWarnings("resource")
    @Test
    public void throwsIfAppHasNotBeenCreated() {
      final var uncreatedApp = new DiasApplication();
      final var activity = mock(Activity.class);
      when(activity.getApplication()).thenReturn(uncreatedApp);
      assertThatThrownBy(() -> requireLauncher(activity))
        .isInstanceOf(IllegalStateException.class);
      clearInvocations(activity);
    }

    @SuppressWarnings("resource")
    @Test
    public void requiresLauncher() {
      final var activity = new Activity();
      final ActivityController<Activity> activityController =
        ActivityController.of(activity);
      requireLauncher(activity);
      activityController.destroy();
    }
  }
}