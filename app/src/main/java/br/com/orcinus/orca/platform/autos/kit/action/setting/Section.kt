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
// • Replaced αὐτός theme by Dias'.

package br.com.orcinus.orca.platform.autos.kit.action.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import br.com.orcinus.orca.platform.autos.kit.action.setting.list.Settings
import br.com.orcinus.orca.platform.autos.kit.action.setting.list.SettingsScope
import br.com.orcinus.orca.platform.autos.kit.action.setting.list.settingsPreviewContent
import com.jeanbarrossilva.dias.app.ui.theme.DiasTheme

@Composable
fun Section(title: String, modifier: Modifier = Modifier, content: SettingsScope.() -> Unit) {
  Column(modifier, Arrangement.spacedBy(DiasTheme.Spacing.s)) {
    Text(
      title.uppercase(),
      Modifier.offset(x = SettingDefaults.Spacing),
      style = DiasTheme.Typography.labelMedium
    )

    Settings(modifier, content)
  }
}

@Composable
@Preview
private fun SectionPreview() = DiasTheme {
  Surface(color = DiasTheme.Colors.background) {
    Section(title = "Section", content = settingsPreviewContent)
  }
}