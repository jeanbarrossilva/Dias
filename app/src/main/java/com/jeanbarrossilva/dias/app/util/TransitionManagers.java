package com.jeanbarrossilva.dias.app.util;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static androidx.transition.TransitionManager.beginDelayedTransition;

import static com.jeanbarrossilva.dias.app.util.MaterialContainerTransforms.setUpTransition;

import android.view.View;
import android.view.ViewGroup;

import androidx.transition.TransitionManager;

import com.google.android.material.transition.MaterialContainerTransform;

/// Extensions for [TransitionManager]s.
public final class TransitionManagers {
  private TransitionManagers() {}

  public static void transition(
    final ViewGroup sceneRoot,
    final MaterialContainerTransform transition,
    final View startView,
    final View endView
  ) {
    if (sceneRoot == null)
      return;
    setUpTransition(transition, startView, endView);
    beginDelayedTransition(sceneRoot, transition);
    if (startView != null && endView != null) {
      startView.setVisibility(GONE);
      endView.setVisibility(VISIBLE);
    }
  }
}