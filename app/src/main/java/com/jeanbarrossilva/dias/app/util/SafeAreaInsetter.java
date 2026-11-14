/*
 * Copyright © 2026 Jean Silva
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *                  https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

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
///
/// @param <Transform> Property or set of properties of a constrained view that
///   will change whenever this insetter is notified of changes to the insets
///   associated to that view.
public final class SafeAreaInsetter<Transform> {
  private View rootView;
  private final Transformer<Transform> transformer;
  private ObjectIntMap<View> constraints;
  private HashMap<View, Transform> anchors;
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
  private final Function1<? super View, Unit> viewTransformResetter;

  /// Defines which properties of views will be transformed according to changes
  /// to insets.
  ///
  /// By default, an insetter will apply margins to its constrained views.
  /// However, depending on the layout or on its specific transitions, doing so
  /// might lead to visual inconsistencies; in these cases, it's recommended
  /// that the translation of the views be changed instead.
  ///
  /// @param <TransformerType> Transformer of the views' properties that change
  ///   whenever a change to insets to which the views were associated is
  ///   notified to the insetter.
  /// @see #margins()
  /// @see #translation()
  public static sealed abstract class Transforming<TransformerType>
    permits Transforming.Margins, Transforming.Translation {
    /// Applies margins to views when insets change.
    public static final class Margins
      extends Transforming<Transformer.Margins> {
      private static final Margins INSTANCE = new Margins();

      /// Private constructor; an instance can be obtained via [#margins()].
      private Margins() {}

      @NonNull
      @Override
      Transformer.Margins createTransformer() {
        return new Transformer.Margins();
      }
    }

    /// Modifies the translation in either or both 2D axes when insets change.
    public static final class Translation
      extends Transforming<Transformer.Translation> {
      private static final Translation INSTANCE = new Translation();

      /// Private constructor; an instance can be obtained via [#translation()].
      private Translation() {}

      @NonNull
      @Override
      Transformer.Translation createTransformer() {
        return new Transformer.Translation();
      }
    };

    @NonNull
    abstract TransformerType createTransformer();

    /// Denotes that the margins of the insetter's views should change whenever
    /// it's notified of an inset change.
    @NonNull
    public static Margins margins() {
      return Margins.INSTANCE;
    }

    /// Denotes that the translation in the X and Y axes of the insetter's views
    /// should change when the insetter's notified of changes to the specified
    /// insets.
    @NonNull
    public static Translation translation() {
      return Translation.INSTANCE;
    }
  }

  private static sealed abstract class Transformer<Transform>
    permits Transformer.Margins, Transformer.Translation {
    private static final class Margins
      extends Transformer<ViewGroup.MarginLayoutParams> {
      @Nullable
      @Override
      protected ViewGroup.MarginLayoutParams get(@NonNull final View view)
        throws NullPointerException {
        final ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        return layoutParams == null
          ? null
          : layoutParams instanceof final ViewGroup.MarginLayoutParams transform
          ? transform
          : cloneWithMargins(layoutParams);
      }

      @Nullable
      @Override
      protected ViewGroup.MarginLayoutParams update(
        @NonNull final Insets insets,
        @Nullable final ViewGroup.MarginLayoutParams anchor,
        @Nullable final ViewGroup.MarginLayoutParams current
      ) throws NullPointerException {
        final ViewGroup.MarginLayoutParams result = cloneWithMargins(current);
        if (result != null) {
          if (anchor instanceof
            final ViewGroup.MarginLayoutParams marginedAnchor) {
            result.leftMargin = marginedAnchor.leftMargin + insets.left;
            result.topMargin = marginedAnchor.topMargin + insets.top;
            result.rightMargin = marginedAnchor.rightMargin + insets.right;
            result.bottomMargin = marginedAnchor.bottomMargin + insets.bottom;
          } else {
            result.leftMargin = insets.left;
            result.topMargin = insets.top;
            result.rightMargin = insets.right;
            result.bottomMargin = insets.bottom;
          }
        }
        return result;
      }

      @Override
      protected void set(
        @NonNull final View view,
        @Nullable final ViewGroup.MarginLayoutParams offset
      ) throws NullPointerException {
        view.setLayoutParams(offset);
      }
    }

    private static final class Translation
      extends Transformer<Translation.Transform> {
      private record Transform(float x, float y) {}

      @NonNull
      @Override
      protected Transform get(@NonNull View view) throws NullPointerException {
        final float translationX = view.getTranslationX();
        final float translationY = view.getTranslationY();
        return new Transform(translationX, translationY);
      }

      @NonNull
      @Override
      protected Transform update(
        @NonNull Insets insets,
        @Nullable Transform anchor,
        @Nullable Transform current
      ) throws NullPointerException {
        final float insetX = insets.right - insets.left;
        final float insetY = insets.top - insets.bottom;
        return anchor == null
          ? new Transform(insetX, insetY)
          : new Transform(anchor.x() + insetX, anchor.y() + insetY);
      }

      @Override
      protected void set(@NonNull View view, @Nullable Transform transform)
        throws NullPointerException {
        if (transform == null)
          return;
        view.setTranslationX(transform.x());
        view.setTranslationY(transform.y());
      }
    }

    public void transform(
      @NonNull final Insets insets,
      @NonNull final View view,
      @Nullable final Transform anchor
    ) throws NullPointerException {
      set(view, update(insets, anchor, get(view)));
    }

    @Nullable
    protected abstract Transform get(@NonNull final View view)
      throws NullPointerException;

    @NonNull
    protected abstract Transform update(
      @NonNull final Insets insets,
      @Nullable final Transform anchor,
      @Nullable final Transform current
    ) throws NullPointerException;

    protected abstract void set(
      @NonNull final View view,
      @Nullable final Transform transform
    ) throws NullPointerException;
  }

  private SafeAreaInsetter(
    @NonNull final Transforming<? extends Transformer<Transform>> transforming
  ) {
    this.transformer = transforming.createTransformer();
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
      final var anchor = transformer.get(view);
      if (anchor != null)
        anchors.put(view, anchor);
      return Unit.INSTANCE;
    };
    this.viewTransformResetter = view -> {
      final Transform anchor = anchors.get(view);
      if (anchor != null)
        transformer.set(view, anchor);
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
  ///   types of insets away from which the view will be pushed. Each mask is
  ///   expected to be one of the constants defined in [WindowInsets.Type], or a
  ///   disjunction of two or more of them (e.g.,
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
      this.constraints.forEachKey(viewTransformResetter);
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
    constraints.forEachKey(viewTransformResetter);
    constraints = null;
    anchors = null;
  }

  /// Instantiates a detached insetter. To observe changes to insets, it must be
  /// attached to a root view afterward.
  ///
  /// @param transforming Defines the properties of the returned insetter's
  ///   constrained views that will be changed upon changes to the insets to
  ///   which these views are associated.
  /// @return The instantiated insetter.
  /// @see #attach(View, ObjectIntMap)
  @NonNull
  @SuppressWarnings({"rawtypes", "unchecked"})
  public static SafeAreaInsetter<?> create(
    @Nullable final Transforming<?> transforming
  ) {
    return transforming == null ? create() : new SafeAreaInsetter(transforming);
  }

  /// Instantiates an insetter that insets its constrained views by changing
  /// their margins. Initially, the insetter will be detached, and must be
  /// attached to a root view in order to observe changes to insets.
  ///
  /// To change other view properties other than the margins, refer to
  /// [#create(Transforming)].
  ///
  /// @return The instantiated insetter.
  /// @see #attach(View, ObjectIntMap)
  @NonNull
  public static SafeAreaInsetter<?> create() {
    return new SafeAreaInsetter<>(Transforming.margins());
  }

  private boolean isAttached() {
    return constraints != null;
  }

  private void inset(@Nullable final Insets insets, @Nullable final View view)
    throws NullPointerException {
    if (insets == null || view == null)
      return;
    transformer.transform(insets, view, anchors.get(view));
  }

  private int requireTypeMask(@NonNull final View view)
    throws NoSuchElementException, NullPointerException {
    return constraints.get(view);
  }
}