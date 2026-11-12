package com.jeanbarrossilva.dias.app.util;

import static android.view.WindowInsetsAnimation.Callback.DISPATCH_MODE_CONTINUE_ON_SUBTREE;
import static com.jeanbarrossilva.dias.app.util.LayoutParams.cloneWithMargins;

import android.graphics.Insets;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsAnimation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ObjectIntMap;

import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/// Deobscures system UI by pushing views away from their specified, respective
/// insets.
public final class SafeAreaInsetter {
  private View rootView;
  private ObjectIntMap<View> constraints;
  private HashMap<View, ViewGroup.LayoutParams> anchors;
  private final View.OnApplyWindowInsetsListener listener;
  private final WindowInsetsAnimation.Callback callback;

  // ObjectIntMap is a Kotlin API, and Java interop doesn't seem to have been
  // taken into account by the API authors. iterating over the keys (the views,
  // in our case) requires a lambda, which will be invoked with each key.
  //
  // these lambdas, in turn, are invoked at every attach or detach. declaring
  // them here allows us to avoid allocating them each time one of those two
  // events happen.
  private final Function1<? super View, Unit> viewAnchorer;
  private final Function1<? super View, Unit> viewMarginResetter;

  /// Instantiates a detached [SafeAreaInsetter].
  ///
  /// @see #attach(View, ObjectIntMap)
  public SafeAreaInsetter() {
    this.constraints = null;
    this.anchors = null;
    this.listener =
      new View.OnApplyWindowInsetsListener() {
        @Nullable
        @Override
        @SuppressWarnings("DataFlowIssue")
        public WindowInsets onApplyWindowInsets(
          @Nullable final View view,
          @Nullable final WindowInsets windowInsets
        ) {
          if (isAttached() && windowInsets != null) {
            if (constraints.containsKey(view)) {
              final Insets insets =
                windowInsets.getInsets(requireTypeMask(view));
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
      };
    this.callback =
      new WindowInsetsAnimation.Callback(DISPATCH_MODE_CONTINUE_ON_SUBTREE) {
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
      };
    this.viewAnchorer = view -> {
      final var anchor = cloneWithMargins(view.getLayoutParams());
      if (anchor != null)
        anchors.put(view, anchor);
      return Unit.INSTANCE;
    };
    this.viewMarginResetter = view -> {
      final ViewGroup.LayoutParams anchor = anchors.get(view);
      if (anchor != null)
        view.setLayoutParams(anchor);
      return Unit.INSTANCE;
    };
  }

  /// Attaches this insetter to the given root view.
  ///
  /// Calling this method is a no-op in case the root view or the constraint
  /// map is `null`.
  ///
  /// @param rootView View to which this insetter will be attached.
  /// @param constraints Views to inset, associated to a mask containing the
  ///   types of insets away from which the view will be pushed. Each
  ///   mask is expected to be one of the constants defined in
  ///   [WindowInsets.Type], or a disjunction of two or more of them (e.g.,
  ///   `navigationBars() | statusBars()`).
  ///
  ///   This map should either be immutable or remain unchanged while this
  ///   insetter is attached. Mutations may cause null-pointer exceptions being
  ///   thrown when insets change or are applied to the root view.
  /// @see WindowInsets.Type#navigationBars()
  /// @see WindowInsets.Type#statusBars()
  /// @see #detach()
  public void attach(
    @Nullable final View rootView,
    @Nullable final ObjectIntMap<View> constraints
  ) {
    if (rootView == null || constraints == null)
      return;
    if (this.constraints != null)
      this.constraints.forEachKey(viewMarginResetter);
    this.rootView = rootView;
    this.constraints = constraints;
    if (constraints.isEmpty())
      anchors = null;
    else {
      anchors = new HashMap<>(constraints.count());
      constraints.forEachKey(viewAnchorer);
    }
    rootView.setWindowInsetsAnimationCallback(callback);
    rootView.setOnApplyWindowInsetsListener(listener);
    rootView.requestApplyInsets();
  }

  /// Requests that the views be pushed away from the insets in the intersection
  /// of this insetter's type mask and the given animation's. The views will
  /// only be inset in case the animation is of one of these insets in the
  /// intersection.
  ///
  /// @param windowInsets Insets of the window.
  /// @param animation Animation according to which the views may be inset.
  /// @return An intersection between this insetter's type mask and that of the
  ///   animation. Essentially, denotes the types of insets about which this
  ///   insetter cares and those pertained to the animation.
  /// @see WindowInsetsAnimation#getTypeMask()
  /// @throws NullPointerException If this insetter is detached or either
  ///   argument is `null`. This insetter can be detached even after a call to
  ///   [#attach(View, ObjectIntMap)] in case the constraint map passed into
  ///   that method was `null`.
  public int inset(
    @NonNull final WindowInsets windowInsets,
    @NonNull final WindowInsetsAnimation animation
  ) throws NullPointerException {
    final var typeMasks = new int[1];
    constraints.forEachKey(view -> {
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

  /// Detaches this insetter from the root view to which it was previously
  /// attached (if any).
  ///
  /// @implNote There's no documented way of checking whether this insetter's
  ///   listener or callback is the view's; therefore, this method sets both to
  ///   `null`. The caller, having set either the listener or the callback to
  ///   something else *after* they've attached us to the view, might expect
  ///   that a call to this method will only unset what *we* have set; it,
  ///   indeed, will *not*.
  /// @see #attach(View, ObjectIntMap)
  public void detach() {
    if (!isAttached())
      return;
    rootView.setOnApplyWindowInsetsListener(null);
    rootView.setWindowInsetsAnimationCallback(null);
    constraints.forEachKey(viewMarginResetter);
    constraints = null;
    anchors = null;
  }

  private boolean isAttached() {
    return constraints != null;
  }

  private void inset(@Nullable final Insets insets, @Nullable final View view)
    throws NullPointerException {
    if (insets == null || view == null || !view.isLaidOut())
      return;
    final ViewGroup.LayoutParams anchor = anchors.get(view);
    final ViewGroup.LayoutParams oldLayoutParams = view.getLayoutParams();
    final var newLayoutParams = oldLayoutParams instanceof
      final ViewGroup.MarginLayoutParams castOldLayoutParams
      ? castOldLayoutParams
      : new ViewGroup.MarginLayoutParams(oldLayoutParams);
    if (anchor instanceof final ViewGroup.MarginLayoutParams castAnchor) {
      newLayoutParams.leftMargin = castAnchor.leftMargin + insets.left;
      newLayoutParams.topMargin = castAnchor.topMargin + insets.top;
      newLayoutParams.rightMargin = castAnchor.rightMargin + insets.right;
      newLayoutParams.bottomMargin = castAnchor.bottomMargin + insets.bottom;
    } else {
      newLayoutParams.leftMargin = insets.left;
      newLayoutParams.topMargin = insets.top;
      newLayoutParams.rightMargin = insets.right;
      newLayoutParams.bottomMargin = insets.bottom;
    }
    view.setLayoutParams(newLayoutParams);
  }

  private int requireTypeMask(@NonNull final View view)
    throws NoSuchElementException, NullPointerException {
    return constraints.get(view);
  }
}