package com.jeanbarrossilva.dias.app.util;

import static android.view.WindowInsets.Type.navigationBars;
import static android.view.WindowInsets.Type.statusBars;
import static androidx.core.view.ViewKt.doOnLayout;

import android.graphics.Insets;
import android.view.View;
import android.view.WindowInsets;

import androidx.annotation.Nullable;

import kotlin.Unit;

/// Listener that pushes views away from the specified insets whenever the
/// window's insets change.
public final class SafeAreaListener
  implements View.OnApplyWindowInsetsListener {
  private final int typeMask;

  /// [SafeAreaListener] that pushes views away from the navigation bars.
  public static final SafeAreaListener navigationBars =
    new SafeAreaListener(navigationBars());

  /// [SafeAreaListener] that pushes views away from the status bars.
  public static final SafeAreaListener statusBars =
    new SafeAreaListener(statusBars());

  /// Instantiates a [SafeAreaListener].
  ///
  /// @param typeMask Mask with the insets away from which views will be pushed.
  ///   Should be one of the constants defined in [WindowInsets.Type], or a
  ///   disjunction of two or more of them (e.g.,
  ///   `navigationBars() | statusBars()`).
  /// @see WindowInsets.Type#navigationBars()
  /// @see WindowInsets.Type#statusBars()
  public SafeAreaListener(final int typeMask) {
    this.typeMask = typeMask;
  }

  @Nullable
  @Override
  public WindowInsets onApplyWindowInsets(
    final View view,
    final WindowInsets windowInsets
  ) {
    if (view != null && windowInsets != null) {
      doOnLayout(view, laidOutView -> {
        final Insets insets = windowInsets.getInsets(typeMask);
        view.setTranslationX(insets.left - insets.right);
        view.setTranslationY(insets.top - insets.bottom);
        return Unit.INSTANCE;
      });
    }
    return windowInsets;
  }
}