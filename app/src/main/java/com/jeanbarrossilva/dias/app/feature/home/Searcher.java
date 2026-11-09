package com.jeanbarrossilva.dias.app.feature.home;

import static android.view.WindowInsets.Type.ime;
import static com.jeanbarrossilva.dias.app.util.MaterialContainerTransforms.setUpTransition;
import static com.jeanbarrossilva.dias.app.util.TransitionManagers.transition;

import android.graphics.Color;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.ComponentActivity;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;

import com.google.android.material.transition.MaterialArcMotion;
import com.google.android.material.transition.MaterialContainerTransform;

import java.io.Closeable;

final class Searcher implements Closeable {
  private final OnBackPressedCallback onBackPressedCallback;
  private final WindowInsetsController.OnControllableInsetsChangedListener
    onControllableInsetsChangedListener;
  private final MaterialContainerTransform transform;
  private final ViewGroup container;
  private final ImageButton button;
  private final EditText field;
  private Runnable onCancellationListener;
  private boolean isCancelled;

  private static final MaterialArcMotion TRANSFORM_PATH_MOTION =
    new MaterialArcMotion();

  public Searcher(
    final ComponentActivity activity,
    final ViewGroup container,
    final ImageButton button,
    final EditText field
  ) {
    this.onBackPressedCallback =
      new OnBackPressedCallback(/* enabled = */ true) {
        @Override
        public void handleOnBackPressed() {
          cancel();
        }
      };
    activity
      .getOnBackPressedDispatcher()
      .addCallback(this.onBackPressedCallback);
    this.onControllableInsetsChangedListener =
      new WindowInsetsController.OnControllableInsetsChangedListener() {
        @Override
        public void onControllableInsetsChanged(
          final WindowInsetsController controller,
          final int typeMask
        ) {
          if ((typeMask & ime()) == 0)
            return;
          final WindowInsets windowInsets = container.getRootWindowInsets();
          if (windowInsets == null || !windowInsets.isVisible(ime()))
            return;
          cancel();
          controller.removeOnControllableInsetsChangedListener(this);
        }
      };
    this.transform = new MaterialContainerTransform();
    this.transform.setPathMotion(TRANSFORM_PATH_MOTION);
    this.transform.setScrimColor(Color.TRANSPARENT);
    this.container = container;
    this.button = button;
    this.field = field;
    this.onCancellationListener = null;
    this.isCancelled = false;
  }

  public void open() {
    final WindowInsetsController windowInsetsController =
      container.getWindowInsetsController();
    setUpTransition(transform, null, null);
    transition(container, transform, button, field);
    field.requestFocus();
    if (windowInsetsController != null) {
      windowInsetsController.show(ime());
      windowInsetsController.addOnControllableInsetsChangedListener(
        onControllableInsetsChangedListener
      );
    }
    isCancelled = false;
  }

  public void setOnCancellationListener(
    @Nullable final Runnable onCancellationListener
  ) {
    this.onCancellationListener = onCancellationListener;
  }

  @Override
  public void close() {
    if (isCancelled)
      return;
    final WindowInsetsController windowInsetsController =
      container.getWindowInsetsController();
    onBackPressedCallback.remove();
    if (windowInsetsController != null)
      windowInsetsController.removeOnControllableInsetsChangedListener(
        onControllableInsetsChangedListener
      );
    setUpTransition(transform, null, null);
    markAsCancelled();
  }

  private void cancel() {
    if (isCancelled)
      return;
    transition(container, transform, field, button);
    markAsCancelled();

  }

  private void markAsCancelled() {
    isCancelled = true;
    if (onCancellationListener != null)
      onCancellationListener.run();
  }
}