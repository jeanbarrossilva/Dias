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
// • Removed internal visibility annotations; and
// • replaced αὐτός theme by Dias'.

package br.com.orcinus.orca.platform.autos.kit.action.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jeanbarrossilva.dias.app.ui.theme.DiasTheme

/** Tag that identifies a [Setting] for testing purposes. */
const val SettingTag = "setting"

/** Tag that identifies a [Setting]'s label for testing purposes. */
const val LabelTag = "setting-label"

/** Default values of a [Setting]. */
internal object SettingDefaults {
  /** [CornerBasedShape] that clips a [Setting] by default. */
  val Shape = RoundedCornerShape(12.dp)

  /** Size in [Dp]s of the default spacing of a [Setting]. */
  val Spacing = DiasTheme.Spacing.m
}

/**
 * A configuration.
 *
 * @param label Short description of what it's for.
 * @param modifier [Modifier] to be applied to the underlying [Row].
 * @param shape [Shape] by which it will be clipped.
 * @param onClick Callback run whenever it's clicked.
 * @param icon [Icon] that visually represents what it does.
 * @param action Portrays the result of invoking [onClick] or executes a related action.
 */
@Composable
internal fun Setting(
  label: @Composable () -> Unit,
  modifier: Modifier = Modifier,
  shape: Shape = SettingDefaults.Shape,
  onClick: () -> Unit = {},
  icon: @Composable () -> Unit = {},
  action: ActionScope.() -> Unit = {}
) {
  Surface(Modifier.fillMaxWidth().testTag(SettingTag), shape) {
    Row(
      Modifier.clickable(onClick = onClick).padding(SettingDefaults.Spacing).then(modifier),
      Arrangement.SpaceBetween,
      Alignment.CenterVertically
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(SettingDefaults.Spacing),
        verticalAlignment = Alignment.CenterVertically
      ) {
        icon()

        Box(Modifier.testTag(LabelTag).semantics(mergeDescendants = true) {}) {
          ProvideTextStyle(
            DiasTheme
              .Typography
              .labelMedium
              .copy(DiasTheme.Colors.onBackground),
            label
          )
        }
      }

      Spacer(Modifier.width(SettingDefaults.Spacing))
      ActionScope().apply(action).content()
    }
  }
}

/** Preview of a [Setting]. */
@Composable
@Preview
private fun SettingPreview() {
  DiasTheme {
    Setting(
      onClick = {},
      label = { Text("Label") },
      icon = {
        Icon(DiasTheme.Icons.Home, contentDescription = "Setting")
      }
    ) {
      icon(contentDescription = { "Expand" }, vector = { ArrowForward })
    }
  }
}