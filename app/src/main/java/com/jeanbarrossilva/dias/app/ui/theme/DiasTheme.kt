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

import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.jeanbarrossilva.dias.app.ui.SpaceGrotesk

/** The UI theme of Dias. */
object DiasTheme {
  private val DefaultTypography: Typography =
    Typography(FontFamily.SpaceGrotesk)

  /** Icon pack of Dias. */
  val Icons = androidx.compose.material.icons.Icons.Rounded

  /** Font and text color and size configuration of Dias. */
  val Typography = Typography(
    FontFamily.SpaceGrotesk,
    bodyMediumEmphasized = DefaultTypography
      .bodyMediumEmphasized
      .copy(fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
  )

  /** Color scheme of Dias. */
  val colors
    @Composable get() = MaterialTheme.colorScheme.copy(
      background = Color.Black,
      onBackground = Color.White,
      primary = Color.White,
      primaryContainer = Color(0xff2b2b2b),
      onPrimaryContainer = Color.White,
      secondaryContainer = Color(0xff202020),
      onSurface = Color.White,
      surfaceVariant = Color(0xff0e0e0e)
    )

  /**
   * Themes the given content with the theme of Dias.
   *
   * @param content Composable to be themed.
   */
  @Composable
  operator fun invoke(content: @Composable () -> Unit) =
    MaterialExpressiveTheme(colors, typography = Typography) {
      Surface(
        color = colors.background,
        contentColor = colors.onBackground,
        content = content
      )
    }
}