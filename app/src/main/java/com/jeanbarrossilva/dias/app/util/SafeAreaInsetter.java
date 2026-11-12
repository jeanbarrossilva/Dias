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
/// @see #attach(View, ObjectIntMap)
public final class SafeAreaInsetter
  extends WindowInsetsAnimation.Callback
  implements View.OnApplyWindowInsetsListener {
  private View container;
  private ObjectIntMap<View> constraints;
  private HashMap<View, Translation> oldTranslations;

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

  /// Instantiates a detached [SafeAreaInsetter].
  public SafeAreaInsetter() {
    super(DISPATCH_MODE_CONTINUE_ON_SUBTREE);
    this.container = null;
    this.constraints = null;
  }

  /// Attaches this insetter to the given view.
  ///
  /// @param constraints The views to be pushed, associated to the mask
  ///   containing the types of insets away from which the respective view will
  ///   be pushed. Each mask is expected to be one of the constants defined in
  ///   [WindowInsets.Type], or a disjunction of two or more of them (e.g.,
  ///   `navigationBars() | statusBars()`).
  /// @param container View to which this insetter will be attached.
  /// @see WindowInsets.Type#navigationBars()
  /// @see WindowInsets.Type#statusBars()
  /// @see #detach()
  public void attach(
    @Nullable final View container,
    @Nullable final ObjectIntMap<View> constraints
  ) {
    if (container == null) {
      detach();
      return;
    }
    if (isAttached())
      detach();
    this.container = container;
    this.constraints = constraints;
    if (constraints == null)
      oldTranslations = null;
    else {
      oldTranslations = new HashMap<>(constraints.count());
      constraints.forEachKey(view -> {
        oldTranslations.put(view, new Translation(view));
        return Unit.INSTANCE;
      });
    }
    container.setWindowInsetsAnimationCallback(this);
    container.setOnApplyWindowInsetsListener(this);
    container.requestApplyInsets();
  }

  @Nullable
  @Override
  public WindowInsets onProgress(
    @Nullable final WindowInsets windowInsets,
    @Nullable final List<WindowInsetsAnimation> runningAnimations
  ) {
    if (isAttached() && windowInsets != null && runningAnimations != null)
      for (final WindowInsetsAnimation animation: runningAnimations)
        inset(windowInsets, animation);
    return windowInsets;
  }

  /// Requests that the views be pushed away from the insets in the union of
  /// this insetter's type mask and the given animation's. The views will only
  /// be pushed in case the animation is of one of these insets.
  ///
  /// @param windowInsets Insets of the window.
  /// @param animation Animation according to which the views may be pushed.
  /// @return A mask containing the types of insets that were consumed.
  /// @see WindowInsetsAnimation#getTypeMask()
  /// @throws NullPointerException If this insetter was attached with `null`
  ///   constraints.
  @NonNull
  public int inset(
    @NonNull final WindowInsets windowInsets,
    @NonNull final WindowInsetsAnimation animation
  ) throws NullPointerException {
    if (!isAttached())
      return 0;
    final ObjectIntMap<View> requiredConstraints =
      requireNonNull(constraints, "constraints");
    final var typeMasks = new int[1];
    requiredConstraints.forEachKey(view -> {
      final int typeMaskIntersection =
        animation.getTypeMask() & requireTypeMask(view);
      if (typeMaskIntersection != 0) {
        final Insets insets = windowInsets.getInsets(typeMaskIntersection);
        inset(insets, view);
        typeMasks[0] |= typeMaskIntersection;
      }
      return Unit.INSTANCE;
    });
    return typeMasks[0];
  }

  @Nullable
  @Override
  public WindowInsets onApplyWindowInsets(
    @Nullable final View view,
    @Nullable final WindowInsets windowInsets
  ) {
    if (isAttached() && windowInsets != null) {
      if (constraints.containsKey(view)) {
        final Insets insets = windowInsets.getInsets(requireTypeMask(view));
        inset(insets, view);
      } else
        constraints.forEachKey(constrainedView -> {
          final Insets insets =
            windowInsets.getInsets(requireTypeMask(constrainedView));
          inset(insets, constrainedView);
          return Unit.INSTANCE;
        });
    }
    return windowInsets;
  }

  /// Detaches this insetter from the view to which it was previously attached
  /// (if any).
  ///
  /// @implNote There is no intuitive way of checking whether the view's
  ///   listener and callback are ourselves; therefore, this method sets both to
  ///   `null`. It may be that the caller, not having attached us to a view or
  ///   having done so, but having set either the listener or the callback to
  ///   something else afterward, will expect that this method will only affect
  ///   what *we* have changed; it, indeed, will *not*.
  /// @see #attach(View, ObjectIntMap)
  public void detach() {
    if (!isAttached())
      return;
    container.setOnApplyWindowInsetsListener(null);
    container.setWindowInsetsAnimationCallback(null);
    container = null;
    if (constraints == null)
      return;
    if (!oldTranslations.isEmpty()) {
      constraints.forEachKey(view -> {
        final Translation oldTranslation = oldTranslations.get(view);
        if (oldTranslation != null)
          oldTranslation.apply(view);
        return Unit.INSTANCE;
      });
    }
    constraints = null;
    oldTranslations = null;
  }

  private boolean isAttached() {
    return container != null && constraints != null;
  }

  private void inset(final Insets insets, final View view) {
    if (insets == null || view == null)
      return;
    doOnLayout(view, laidOutView -> {
      laidOutView.setTranslationX(insets.left - insets.right);
      laidOutView.setTranslationY(insets.top - insets.bottom);
      return Unit.INSTANCE;
    });
  }

  private int requireTypeMask(final View view)
    throws NoSuchElementException, NullPointerException {
    return constraints.get(view);
  }
}