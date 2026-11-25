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

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class ChangeEffectTests {
  @JvmField
  @Rule
  val composeRule = createComposeRule()

  @Test
  fun callsCallbackOnValueChange() {
    var value by mutableStateOf(false)
    val change = arrayOfNulls<Boolean>(2)
    var changeCount = 0
    composeRule.setContent {
      ChangeEffect(value) { previous, current ->
        change[0] = previous
        change[1] = current
        changeCount++
      }

      LaunchedEffect(Unit) {
        value = true
      }
    }
    assertThat(change).describedAs("change").containsExactly(false, true)
    assertThat(changeCount).describedAs("changeCount").isEqualTo(1)
  }

  @Test
  fun doesNotCallCallbackWhenValueChangesToAnEqualValue() {
    var value by mutableStateOf(false)
    var changeCount = 0
    composeRule.setContent {
      ChangeEffect(value) { _, _ ->
        changeCount++
      }

      LaunchedEffect(Unit) {
        repeat(2) {
          value = true
        }
      }
    }
    assertThat(changeCount).describedAs("changeCount").isEqualTo(1)
  }
}