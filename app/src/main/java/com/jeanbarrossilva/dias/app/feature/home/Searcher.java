package com.jeanbarrossilva.dias.app.feature.home;

import static com.jeanbarrossilva.dias.app.util.MaterialContainerTransforms.setUpTransition;
import static com.jeanbarrossilva.dias.app.util.TransitionManagers.transition;
import static com.jeanbarrossilva.dias.app.util.Views.requestFocusWithIme;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.ComponentActivity;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;

import com.google.android.material.transition.MaterialArcMotion;
import com.google.android.material.transition.MaterialContainerTransform;

import org.apache.commons.lang3.function.BooleanConsumer;

import java.io.Closeable;

final class Searcher implements Closeable {
  private OnBackPressedCallback onBackPressedCallback;
  private final MaterialContainerTransform transform;
  private final ViewGroup container;
  private final View origin;
  private final View target;
  private BooleanConsumer onToggleListener;

  private static final MaterialArcMotion TRANSFORM_PATH_MOTION =
    new MaterialArcMotion();

  public Searcher(
    final ViewGroup container,
    final View origin,
    final View target
  ) {
    this.transform = new MaterialContainerTransform();
    this.transform.setPathMotion(TRANSFORM_PATH_MOTION);
    this.transform.setScrimColor(Color.TRANSPARENT);
    this.container = container;
    this.origin = origin;
    this.target = target;
    this.onToggleListener = null;
  }

  public void open(final ComponentActivity activity) {
    if (onBackPressedCallback == null)
      onBackPressedCallback =
        new OnBackPressedCallback(/* enabled = */ true) {
          @Override
          public void handleOnBackPressed() {
            cancel();
          }
        };
    else
      onBackPressedCallback.remove();
    activity.getOnBackPressedDispatcher().addCallback(onBackPressedCallback);
    setUpTransition(transform, null, null);
    transition(container, transform, origin, target);
    requestFocusWithIme(target);
    notifyOnToggleListener(true);
  }

  public void setOnToggleListener(
    @Nullable final BooleanConsumer onToggleListener
  ) {
    this.onToggleListener = onToggleListener;
  }

  @Override
  public void close() {
    removeCallback();
    notifyOnToggleListener(false);
    setUpTransition(transform, null, null);
  }

  private void cancel() {
    removeCallback();
    notifyOnToggleListener(false);
    transition(container, transform, target, origin);
  }

  private void removeCallback() {
    if (onBackPressedCallback == null)
      return;
    onBackPressedCallback.remove();
  }

  private void notifyOnToggleListener(final boolean isSearching) {
    if (onToggleListener == null)
      return;
    onToggleListener.accept(isSearching);
  }
}