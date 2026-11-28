@file:JvmName("LazyListScopes")

package com.jeanbarrossilva.dias.app.ui

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

private object Spacing

/**
 * Includes a space intended to separate an items in the list from another item
 * or either list's bounds.
 *
 * @param orientation The orientation of the list: horizontal or vertical.
 * @param size Amount of DPs by which both items will be separated.
 */
internal fun LazyListScope.spacing(orientation: Orientation, size: Dp) =
  item(contentType = Spacing) {
    Spacer(when (orientation) {
      Orientation.Horizontal -> Modifier.width(size)
      Orientation.Vertical -> Modifier.height(size)
    })
  }