package com.jeanbarrossilva.dias.app.home;

import android.app.Activity;
import android.graphics.RectF;
import android.view.View;
import android.widget.FrameLayout;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

import java.util.concurrent.TimeoutException;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static com.jeanbarrossilva.dias.app.home.Instrumentations.waitForSize;
import static com.jeanbarrossilva.dias.app.home.Views.getBoundsInParent;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Suite.class)
@Suite.SuiteClasses(ViewsTests.BoundsInRectTests.class)
public class ViewsTests {
  @RunWith(RobolectricTestRunner.class)
  public static final class BoundsInRectTests {
    @Test
    public void outputRectRemainsUnchangedWhenViewIsNull() {
      final var emptyOut = new RectF();
      final var actualOut = new RectF();
      getBoundsInParent(null, actualOut);
      assertThat(actualOut).isEqualTo(emptyOut);
    }

    @Test
    public void getsBoundInParent()
      throws InterruptedException, TimeoutException {
      final ActivityController<Activity> activityController =
        ActivityController.of(new Activity());
      final var view = new View(getApplicationContext());
      final var out = new RectF();
      activityController.setup();
      activityController.get().setContentView(view);
      view.setX(2);
      view.setY(4);
      view.setLayoutParams(
        new FrameLayout.LayoutParams(/* width = */ 128, /* height = */ 256)
      );
      waitForSize(getInstrumentation(), view);
      getBoundsInParent(view, out);
      assertThat(out).isEqualTo(
        new RectF(
          /* left = */ view.getX(),
          /* top = */ view.getY(),
          /* right = */ view.getX() + view.getWidth(),
          /* bottom = */ view.getY() + view.getHeight()
        )
      );
      activityController.close();
    }
  }
}