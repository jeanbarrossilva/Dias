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

import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.Icons as MaterialIcons
import androidx.compose.material.icons.automirrored.rounded.ArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeanbarrossilva.dias.app.ui.SpaceGrotesk

/** The UI theme of Dias. */
object DiasTheme {
  private val DefaultFontFamily = FontFamily.SpaceGrotesk
  private val DefaultTypography: Typography = Typography(DefaultFontFamily)
  private val OutlineColor = Color(0xff0e0e0e)

  /** Color scheme of Dias. */
  val Colors = ColorScheme(
    primary = Color.White,
    onPrimary = Color.Unspecified,
    primaryContainer = Color(0xff2b2b2b),
    onPrimaryContainer = Color.White,
    inversePrimary = Color.Unspecified,
    secondary = Color.Unspecified,
    onSecondary = Color.Unspecified,
    secondaryContainer = Color(0xff202020),
    onSecondaryContainer = Color.Unspecified,
    tertiary = Color.Unspecified,
    onTertiary = Color.Unspecified,
    tertiaryContainer = Color.Unspecified,
    onTertiaryContainer = Color.Unspecified,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.Unspecified,
    onSurface = Color.White,
    surfaceVariant = Color.Unspecified,
    onSurfaceVariant = Color(0xffe0e0e0),
    surfaceTint = Color.Unspecified,
    inverseSurface = Color.Unspecified,
    inverseOnSurface = Color.Unspecified,
    error = Color.Unspecified,
    onError = Color.Unspecified,
    errorContainer = Color.Unspecified,
    onErrorContainer = Color.Unspecified,
    outline = OutlineColor,
    outlineVariant = OutlineColor,
    scrim = Color.Unspecified,
    surfaceBright = Color.Unspecified,
    surfaceDim = Color.Unspecified,
    surfaceContainer = Color(0xff1a1a1a),
    surfaceContainerHigh = Color.Unspecified,
    surfaceContainerHighest = Color.Unspecified,
    surfaceContainerLow = Color.Unspecified,
    surfaceContainerLowest = Color.Unspecified,
    primaryFixed = Color.Unspecified,
    primaryFixedDim = Color.Unspecified,
    onPrimaryFixed = Color.Unspecified,
    onPrimaryFixedVariant = Color.Unspecified,
    secondaryFixed = Color.Unspecified,
    secondaryFixedDim = Color.Unspecified,
    onSecondaryFixed = Color.Unspecified,
    onSecondaryFixedVariant = Color.Unspecified,
    tertiaryFixed = Color.Unspecified,
    tertiaryFixedDim = Color.Unspecified,
    onTertiaryFixed = Color.Unspecified,
    onTertiaryFixedVariant = Color.Unspecified,
  )

  /** Font and text color and size configuration of Dias. */
  val Typography = Typography(
    DefaultFontFamily,
    displaySmall = DefaultTypography
      .displaySmall
      .copy(fontWeight = FontWeight.ExtraBold),
    bodyMediumEmphasized = DefaultTypography
      .bodyMediumEmphasized
      .copy(fontSize = 18.sp, fontWeight = FontWeight.ExtraBold),
    labelMedium = DefaultTypography
      .labelMedium
      .copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
    labelSmall = DefaultTypography
      .labelMedium
      .copy(fontSize = 14.sp, lineHeight = 18.sp),
  )

  /** Icon pack of Dias. */
  object Icons {
    /** Denotes the addition of something (e.g., of an item to a list). */
    val Add = MaterialIcons.Rounded.Add

    /** Denotes the continuation of a context, or points to a UI element. */
    val ArrowForward = MaterialIcons.AutoMirrored.Rounded.ArrowRight

    /** Denotes an initial page or state. */
    val Home = MaterialIcons.Rounded.Home

    /** Denotes a relation between two concepts. */
    val Link = MaterialIcons.Rounded.Link

    /** Denotes a return to a previous context. */
    val Return = MaterialIcons.AutoMirrored.Rounded.ArrowBack

    /** Denotes a lookup regarding some data or section of Dias. */
    val Search = MaterialIcons.Rounded.Search
  }

  /** Standard amounts of space in Dias. */
  object Spacing {
    /** Extra small spacing. */
    val xs = 4.dp

    /** Small spacing. */
    val s = 8.dp

    /** Medium spacing. */
    val m = 16.dp

    /** Large spacing. */
    val l = 24.dp

    /** Extra large spacing. */
    val xl = 32.dp
  }

  /**
   * Themes the given content with the theme of Dias.
   *
   * @param content Composable to be themed.
   */
  @Composable
  operator fun invoke(content: @Composable () -> Unit) =
    MaterialExpressiveTheme(Colors, typography = Typography, content = content)
}