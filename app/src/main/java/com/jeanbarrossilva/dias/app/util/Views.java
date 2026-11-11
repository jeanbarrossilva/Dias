package com.jeanbarrossilva.dias.app.util;

import static android.view.WindowInsets.Type.ime;

import android.annotation.SuppressLint;
import android.graphics.RectF;
import android.view.View;
import android.view.WindowInsetsController;

import java.lang.reflect.Field;

/** Extensions for {@link View}s. */
public final class Views {
  private Views() {}

  /**
   * Updates the given rectangle to the position and the size of the view
   * relative to the view's parent.
   *
   * @param self View whose parent-relative bounds will be set to the rectangle.
   * @param out Rectangle to be updated to represent the view's, relative to the
   *   view's parent.
   */
  public static void getBoundsInParent(final View self, final RectF out) {
    if (self == null || out == null)
      return;
    final float x = self.getX();
    final float y = self.getY();
    out.left = x;
    out.top = y;
    out.right = x + self.getWidth();
    out.bottom = y + self.getHeight();
  }

  /// Returns the listener that'll be notified of each click on the given view.
  ///
  /// @author Kevin Coppock
  /// @implNote This method's implementation is fragile, as it relies on
  ///   accessing private classes and fields via reflection; it may break in
  ///   future Android releases.
  /// @param view View whose on-click listener will be returned.
  /// @return The view's on-click listener, or `null` if the view is `null`.
  @SuppressLint({"DiscouragedPrivateApi", "PrivateApi"})
  @SuppressWarnings("JavaReflectionMemberAccess")
  public static View.OnClickListener getOnClickListener(final View view) {
    /*
     * Copyright © 2013 Kevin Coppock
     * https://stackoverflow.com/a/14993764/10252241
     *
     * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0
     * Unported License. For a copy of this license, refer to
     * https://creativecommons.org/licenses/by-sa/3.0.
     */

    // Dias-specific changes:
    // • Null-check view;
    // • reformatted code, and inlined and renamed variables;
    // • removed code path for API level < 14 (Android 4.0 Ice Cream Sandwich);
    //   and
    // • removed logs.

    if (view == null)
      return null;
    try {
      final Field infoField = View.class.getDeclaredField("mListenerInfo");
      infoField.setAccessible(true);
      final Object listenerInfo = infoField.get(view);
      if (listenerInfo != null) {
        return (View.OnClickListener)
          Class.forName("android.view.View$ListenerInfo")
            .getDeclaredField("mOnClickListener")
            .get(listenerInfo);
      }
    } catch (final ClassNotFoundException
                 | IllegalAccessException
                 | NoSuchFieldException cause) {
      throw new RuntimeException(cause);
    }
    return null;
  }

  /// Requests that the given view be focused and shows the IME.
  ///
  /// @param self View for which focus will be requested.
  public static void requestFocusWithIme(final View self) {
    if (self == null)
      return;
    final WindowInsetsController windowInsetsController =
      self.getWindowInsetsController();
    self.requestFocus();
    if (windowInsetsController != null)
      windowInsetsController.show(ime());
  }

  /// Sets the bottom padding of the given view.
  ///
  /// @param self View whose bottom padding will be set.
  /// @param paddingBottom Amount of padding to be applied to the bottom of the
  ///   view.
  public static void setPaddingBottom(
    final View self,
    final int paddingBottom
  ) {
    if (self == null)
      return;
    self.setPadding(
      self.getPaddingLeft(),
      self.getPaddingTop(),
      self.getPaddingRight(),
      paddingBottom
    );
  }
}