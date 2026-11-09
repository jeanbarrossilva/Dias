package com.jeanbarrossilva.dias.app.util;

import static androidx.core.view.ViewKt.doOnLayout;

import android.graphics.Insets;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsAnimation;

import androidx.annotation.Nullable;

import java.util.List;

import kotlin.Unit;

/// Pushes views away from the specified insets whenever the window's insets
/// change.
///
/// @implNote This is both an on-apply-window-insets listener and an animation
///   callback. Why? Because some views need an initial push away from these
///   insets, and such initial push might not occur if we're only relying on
///   notifications to the callback. We work around this by listening to the
///   *first* time in which these insets are applied (given that listening to
///   subsequent ones would push the view away *too much*).
/// @see #attach(View)
public final class SafeAreaPusher
  extends WindowInsetsAnimation.Callback
  implements View.OnApplyWindowInsetsListener {
  private View container;
  private final View[] views;
  private final float[] oldTranslations;
  private final int typeMask;
  private boolean wereWindowInsetsApplied;

  /// Instantiates a [SafeAreaPusher].
  ///
  /// @param views Views to be pushed.
  /// @param typeMask Mask with the insets away from which views will be pushed.
  ///   Should be one of the constants defined in [WindowInsets.Type], or a
  ///   disjunction of two or more of them (e.g.,
  ///   `navigationBars() | statusBars()`).
  /// @see WindowInsets.Type#navigationBars()
  /// @see WindowInsets.Type#statusBars()
  public SafeAreaPusher(@Nullable final View[] views, final int typeMask) {
    super(DISPATCH_MODE_STOP);
    this.container = null;
    this.views = views;
    if (this.views == null)
      this.oldTranslations = null;
    else {
      this.oldTranslations = new float[this.views.length * 2];
      for (int index = 0; index < this.views.length; index++) {
        oldTranslations[index * 2] = this.views[index].getTranslationX();
        oldTranslations[index * 2 + 1] = this.views[index].getTranslationY();
      }
    }
    this.typeMask = typeMask;
    this.wereWindowInsetsApplied = false;
  }

  /// Attaches this pusher to the given view.
  ///
  /// @param container View to which this pusher will be attached.
  /// @see #detach()
  public void attach(@Nullable final View container) {
    this.container = container;
    if (this.container == null)
      return;
    this.container.setWindowInsetsAnimationCallback(this);
    this.container.setOnApplyWindowInsetsListener(this);
  }

  @Nullable
  @Override
  public WindowInsets onProgress(
    @Nullable final WindowInsets windowInsets,
    @Nullable final List<WindowInsetsAnimation> runningAnimations
  ) {
    if (views != null && windowInsets != null && runningAnimations != null)
      for (final WindowInsetsAnimation animation: runningAnimations) {
        if ((animation.getTypeMask() & typeMask) == 0)
          continue;
        final Insets insets = windowInsets.getInsets(typeMask);
        for (final View view: views)
          push(view, insets);
      }
    return windowInsets;
  }

  @Nullable
  @Override
  public WindowInsets onApplyWindowInsets(
    @Nullable final View view,
    @Nullable final WindowInsets windowInsets
  ) {
    if (!wereWindowInsetsApplied && windowInsets != null) {
      push(view, windowInsets.getInsets(typeMask));
      wereWindowInsetsApplied = true;
    }
    return windowInsets;
  }

  /// Detaches this pusher from the view to which it was previously attached (if
  /// it was).
  ///
  /// @implNote There is no intuitive way of checking whether the view's
  ///   listener and callback are ourselves; therefore, this method sets both to
  ///   `null`. It may be that the caller, not having attached this pusher to a
  ///   view or having done so, but setting either the listener or the callback
  ///   to something else afterward, will expect that this method will only
  ///   affect what *we* have changed; it, indeed, will *not*.
  /// @see #attach(View)
  public void detach() {
    if (container == null)
      return;
    container.setOnApplyWindowInsetsListener(null);
    container.setWindowInsetsAnimationCallback(null);
    container = null;
    if (views == null)
      return;
    for (int index = 0; index < views.length; index++) {
      views[index].setTranslationX(oldTranslations[index * 2]);
      views[index].setTranslationY(oldTranslations[index * 2 + 1]);
    }
  }

  private void push(final View view, final Insets insets) {
    if (view == null)
      return;
    doOnLayout(view, laidOutView -> {
      laidOutView.setTranslationX(insets.left - insets.right);
      laidOutView.setTranslationY(insets.top - insets.bottom);
      return Unit.INSTANCE;
    });
  }
}