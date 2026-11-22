/*
 * Copyright © 2026 Jean Silva
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see https://www.gnu.org/licenses.
 */

package com.jeanbarrossilva.dias.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush.Companion.linearGradient
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/** Dias' implementation of the Material floating action button (FAB). */
object FloatingActionButton {
  /** Brush of a Dias FAB's background. */
  val containerBrush
    @Composable get() = linearGradient(
      listOf(DiasTheme.colors.secondaryContainer, mainContainerColor)
    )

  /** Shape of a Dias' FAB. */
  val Shape = RoundedCornerShape(24.dp)

  /** Expected spacing between a Dias FAB and its parent layout's bounds. */
  val Margin = 16.dp

  private val mainContainerColor
    @Composable get() = DiasTheme.colors.primaryContainer

  /**
   * The FAB composable.
   *
   * @param onClick Lambda invoked whenever this FAB is clicked.
   * @param modifier Modifier for this FAB.
   * @param content Composable to be shown in this FAB.
   */
  @Composable
  operator fun invoke(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
  ) {
    Box(
      modifier
        .border(2.dp, DiasTheme.colors.surfaceVariant, Shape)
        .clip(Shape)
        .clickable(onClick = onClick, role = Role.Button)
        .background(containerBrush, Shape)
        .size(56.dp),
      Alignment.Center
    ) {
      CompositionLocalProvider(
        LocalContentColor provides contentColorFor(mainContainerColor),
        content
      )
    }
  }
}

@Composable
@Preview
private fun FloatingActionButtonPreview() = DiasTheme {
  FloatingActionButton(onClick = {}) {
    Icon(DiasTheme.Icons.Add, contentDescription = "Add")
  }
}