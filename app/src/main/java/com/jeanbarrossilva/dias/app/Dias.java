package com.jeanbarrossilva.dias.app;

import android.app.Activity;

import com.jeanbarrossilva.dias.launcher.Launcher;

import java.io.Closeable;

import static java.util.Objects.requireNonNull;

/**
 * Static utilities for global, Dias-specific operations.
 * <p>
 * <b>NOTE</b>: None of the objects returned by this class should be closed
 * other than by the running {@link DiasApplication} instance. They're used by
 * various parts of Dias at different stages of its components' lifecycle; other
 * classes closing these objects might result in the app being in an undefined
 * state and/or crashing.
 *
 * @see Closeable#close()
 */
public final class Dias {
  private Dias() {}

  /**
   * Returns the global launcher.
   *
   * @param activity Activity from which the launcher will be found.
   * @return The non-{@code null}, global launcher.
   * @throws ClassCastException If the app instance isn't a
   *   {@link DiasApplication}.
   * @throws IllegalStateException If the app hasn't been created yet or has
   *   been terminated.
   * @throws NullPointerException If the activity is {@code null}.
   */
  public static Launcher requireLauncher(final Activity activity)
    throws ClassCastException, NullPointerException {
    final DiasApplication app = requireNonNull(
      ((DiasApplication) requireNonNull(activity, "activity").getApplication()),
      "activity.getApplication()"
    );
    switch (app.getLifecycleState()) {
      case INITIALIZED ->
        throw new IllegalStateException("App hasn't been created yet.");
      case DESTROYED ->
        throw new IllegalStateException("App has been terminated.");
    }
    return app.launcher;
  }
}