package com.jeanbarrossilva.dias.app.util;

import static androidx.core.view.ViewKt.doOnLayout;

import static java.util.Objects.requireNonNull;

import android.graphics.Insets;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsAnimation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ObjectIntMap;

import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

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
public final class SafeAreaListener
  extends WindowInsetsAnimation.Callback
  implements View.OnApplyWindowInsetsListener {
  private View container;
  private ObjectIntMap<View> constraints;
  private final HashMap<View, Translation> oldTranslations;
  private boolean wereWindowInsetsApplied;

  private static final class Translation {
    public final float x;
    public final float y;

    public Translation(final View view) {
      this.x = view.getTranslationX();
      this.y = view.getTranslationY();
    }

    public void apply(final View view) {
      view.setTranslationX(x);
      view.setTranslationY(y);
    }
  }

  /// Instantiates a [SafeAreaListener].
  ///
  /// @param constraints The views to be pushed, associated to the mask
  ///   containing the types of insets away from which the respective view will
  ///   be pushed. Each mask is expected to be one of the constants defined in
  ///   [WindowInsets.Type], or a disjunction of two or more of them (e.g.,
  ///   `navigationBars() | statusBars()`).
  /// @see WindowInsets.Type#navigationBars()
  /// @see WindowInsets.Type#statusBars()
  public SafeAreaListener(@Nullable final ObjectIntMap<View> constraints) {
    super(DISPATCH_MODE_CONTINUE_ON_SUBTREE);
    this.container = null;
    this.constraints = constraints;
    if (this.constraints == null)
      this.oldTranslations = null;
    else {
      this.oldTranslations = new HashMap<>(constraints.count());
      forEachView(view -> oldTranslations.put(view, new Translation(view)));
    }
  }

  /// Attaches this listener to the given view.
  ///
  /// @param container View to which this listener will be attached.
  /// @see #detach()
  public void attach(@Nullable final View container) {
    this.container = container;
    if (!isAttached())
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
    if (isAttached() && windowInsets != null && runningAnimations != null)
      for (final WindowInsetsAnimation animation: runningAnimations)
        offsetViews(windowInsets, animation);
    return windowInsets;
  }

  /// Requests that the views be pushed away from the insets in the union of
  /// this listener's type mask and the given animation's. The views will only
  /// be pushed in case the animation is of one of these insets.
  ///
  /// @param windowInsets Insets of the window.
  /// @param animation Animation according to which the views may be pushed.
  /// @return Mask with the types of insets away from which the views were
  ///   pushed.
  /// @see WindowInsetsAnimation#getTypeMask()
  /// @throws NullPointerException If this listener is attached, but was
  ///   instantiated with a `null` map of constraints.
  public int offsetViews(
    @NonNull final WindowInsets windowInsets,
    @NonNull final WindowInsetsAnimation animation
  ) throws NullPointerException {
    var results = new int[1];
    if (isAttached()) {
      requireNonNull(constraints, "constraints");
      forEachView(view -> {
        final Integer typeMask = getTypeMask(view);
        if (typeMask == null || (animation.getTypeMask() & typeMask) == 0)
          return;
        final Insets insets = windowInsets.getInsets(typeMask);
        offsetView(insets, view);
        results[0] |= animation.getTypeMask();
      });
    }
    return results[0];
  }

  @Nullable
  @Override
  public WindowInsets onApplyWindowInsets(
    @Nullable final View view,
    @Nullable final WindowInsets windowInsets
  ) {
    if (isAttached() && !wereWindowInsetsApplied && windowInsets != null) {
      final Integer typeMask = getTypeMask(view);
      if (typeMask != null) {
        final Insets insets = windowInsets.getInsets(typeMask);
        offsetView(insets, view);
        wereWindowInsetsApplied = true;
      }
    }
    return windowInsets;
  }

  /// Detaches this listener from the view to which it was previously attached
  /// (if any).
  ///
  /// @implNote There is no intuitive way of checking whether the view's
  ///   listener and callback are ourselves; therefore, this method sets both to
  ///   `null`. It may be that the caller, not having attached this listener
  ///   to a view or having done so, but setting either the listener or the
  ///   callback to something else afterward, will expect that this method will
  ///   only affect what *we* have changed; it, indeed, will *not*.
  /// @see #attach(View)
  public void detach() {
    if (container == null)
      return;
    container.setOnApplyWindowInsetsListener(null);
    container.setWindowInsetsAnimationCallback(null);
    container = null;
    if (constraints == null)
      return;
    forEachView(view -> {
      final Translation oldTranslation = oldTranslations.get(view);
      if (oldTranslation == null)
        return;
      oldTranslation.apply(view);
    });
    constraints = null;
  }

  @SuppressWarnings("KotlinInternalInJava")
  private void forEachView(final Consumer<View> consumer) {
    for (int index = 0; index < constraints.count(); index++) {
      final View view = (View) constraints.keys[index];
      if (view == null)
        continue;
      consumer.accept(view);
    }
  }

  private boolean isAttached() {
    return container != null && constraints != null && constraints.isNotEmpty();
  }

  private void offsetView(final Insets insets, final View view) {
    if (insets == null || view == null)
      return;
    doOnLayout(view, laidOutView -> {
      laidOutView.setTranslationX(insets.left - insets.right);
      laidOutView.setTranslationY(insets.top - insets.bottom);
      return Unit.INSTANCE;
    });
  }

  private Integer getTypeMask(final View view) {
    try {
      return constraints == null ? null : constraints.get(view);
    } catch (final NoSuchElementException exception) {
      return null;
    }
  }
}