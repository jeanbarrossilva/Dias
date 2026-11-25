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

package com.jeanbarrossilva.dias.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Effect that observes changes to a state's value and allows for comparing its
 * old, previous instance to the new one, to which the state was changed. The
 * two values are deemed different and, therefore, the callback is called, if
 * `previous.equals(current)` is `false`.
 *
 * @param value Current value of the state.
 * @param onChange Callback called whenever the given value changes.
 */
@Composable
fun <Value> ChangeEffect(
  value: Value,
  onChange: (previous: Value, current: Value) -> Unit
) {
  var previous by remember { mutableStateOf(value) }

  DisposableEffect(value) {
    if (previous != value) {
      onChange(previous, value)
      previous = value
    }
    onDispose {}
  }
}