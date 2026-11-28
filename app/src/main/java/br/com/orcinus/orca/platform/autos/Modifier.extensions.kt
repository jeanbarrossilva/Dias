/*
 * Copyright © 2023–2024 Orcinus
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see https://www.gnu.org/licenses.
 */

// Dias-specific changes:
// • Removed references to αὐτός borders; and
// • replaced αὐτός theme by Dias'.

package br.com.orcinus.orca.platform.autos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.jeanbarrossilva.dias.app.ui.theme.DiasTheme

/**
 * Applies a [BorderStroke], shaped by the given [shape], when they are applicable.
 *
 * @param shape [Shape] of the [BorderStroke] to be applied.
 */
fun Modifier.border(shape: Shape): Modifier {
  return composed {
    if (isSystemInDarkTheme())
      this
    else
      border(2.dp, DiasTheme.Colors.outline, shape)
  }
}