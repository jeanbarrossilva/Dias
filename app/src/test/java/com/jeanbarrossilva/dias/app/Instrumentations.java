package com.jeanbarrossilva.dias.app;

import android.app.Instrumentation;
import android.view.View;

import androidx.test.espresso.IdlingPolicies;
import androidx.test.espresso.IdlingPolicy;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** Extensions for {@link Instrumentation}s. */
final class Instrumentations {
  private Instrumentations() {}

  /**
   * Blocks the current thread until both dimensions of the give view aren't
   * zero.
   *
   * @param self Instrumentation through which the view's dimensions will be
   *   queried and listened to on the UI thread.
   * @param view View whose dimensions will be waited for.
   * @throws InterruptedException If the current thread is interrupted.
   * @throws TimeoutException If this function waits for more than the duration
   *   defined by Espresso's master idling policy.
   */
  public static void waitForSize(final Instrumentation self, final View view)
    throws InterruptedException, TimeoutException {
    if (self == null || view == null)
      return;
    final var latch = new CountDownLatch(1);
    self.runOnMainSync(() -> {
      if (isSized(view)) {
        latch.countDown();
        return;
      }
      view.addOnLayoutChangeListener(
        new View.OnLayoutChangeListener() {
          @Override
          public void onLayoutChange(
            final View v,
            final int left,
            final int top,
            final int right,
            final int bottom,
            final int oldLeft,
            final int oldTop,
            final int oldRight,
            final int oldBottom
          ) {
            if (!isSized(view))
              return;
            v.removeOnLayoutChangeListener(this);
            latch.countDown();
          }
        }
      );
    });
    final IdlingPolicy idlingPolicy = IdlingPolicies.getMasterIdlingPolicy();
    final long timeout = idlingPolicy.getIdleTimeout();
    final TimeUnit timeoutUnit = idlingPolicy.getIdleTimeoutUnit();
    final boolean didTimeout = !latch.await(timeout, timeoutUnit);
    if (didTimeout)
      throw new TimeoutException(
        "view remained unsized for "
          + timeout
          + ' '
          + switch (timeoutUnit) {
            case TimeUnit.DAYS -> "d";
            case TimeUnit.HOURS -> "h";
            case TimeUnit.MINUTES -> "min";
            case TimeUnit.SECONDS -> "s";
            case TimeUnit.MILLISECONDS -> "ms";
            case TimeUnit.MICROSECONDS -> "μs";
            case TimeUnit.NANOSECONDS -> "ns";
          }
      );
  }

  private static boolean isSized(final View view) {
    return view.getWidth() > 0 && view.getHeight() > 0;
  }
}