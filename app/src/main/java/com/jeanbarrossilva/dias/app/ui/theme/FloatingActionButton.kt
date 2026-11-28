/*
 * Copyright © 2026 Jean Silva
 *
 * Dias is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 2 of the License, or (at your option) any later
 * version.
 *
 * Dias is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Dias. If not, see https://www.gnu.org/licenses.
 */

package com.jeanbarrossilva.dias.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
  val containerBrush = linearGradient(
      listOf(DiasTheme.Colors.secondaryContainer, MainContainerColor)
    )

  /** Shape of a Dias' FAB. */
  val Shape = RoundedCornerShape(24.dp)

  /** Expected spacing between a Dias FAB and its parent layout's bounds. */
  val Margin = 16.dp

  private val MainContainerColor
    get() = DiasTheme.Colors.primaryContainer

  /**
   * The FAB composable.
   *
   * @param onClick Lambda invoked whenever this FAB receives a single click.
   * @param onClickLabel Description of the on-click action.
   * @param onLongClick Lambda invoked whenever this FAB is pressed.
   * @param onLongClickLabel Description of the on-long-click action.
   * @param modifier Modifier for this FAB.
   * @param content Composable to be shown in this FAB.
   */
  @Composable
  operator fun invoke(
    onClick: () -> Unit,
    onClickLabel: String,
    onLongClick: () -> Unit,
    onLongClickLabel: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
  ) {
    Box(
      Modifier
        .border(2.dp, DiasTheme.Colors.outline, Shape)
        .clip(Shape)
        .combinedClickable(
          onClickLabel = onClickLabel,
          role = Role.Button,
          onLongClickLabel = onLongClickLabel,
          onLongClick = onLongClick,
          onClick = onClick
        )
        .background(containerBrush, Shape)
        .size(56.dp)
        .then(modifier),
      Alignment.Center
    ) {
      CompositionLocalProvider(
        LocalContentColor provides contentColorFor(MainContainerColor),
        content
      )
    }
  }
}

@Composable
@Preview
private fun FloatingActionButtonPreview() = DiasTheme {
  FloatingActionButton(
    onClick = {},
    onClickLabel = "Add",
    onLongClick = {},
    onLongClickLabel = "No-op"
  ) {
    Icon(DiasTheme.Icons.Add, contentDescription = "Add")
  }
}