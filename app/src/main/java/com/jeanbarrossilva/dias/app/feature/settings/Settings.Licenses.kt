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

package com.jeanbarrossilva.dias.app.feature.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import br.com.orcinus.orca.platform.autos.kit.action.setting.list.Settings
import com.jeanbarrossilva.dias.app.feature.settings.SettingsDefaults.Padding
import com.jeanbarrossilva.dias.app.ui.theme.DiasTheme

@Composable
internal fun Licenses(
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues.Zero
) = Settings(
  modifier
    .verticalScroll(rememberScrollState())
    .padding(contentPadding)
) {
  License.entries.forEach { license ->
    group(
      icon = {},
      label = { Text(license.title) },
      isInitiallyExpanded = true
    ) {
      license.notices.forEach { notice ->
        setting({ Text(notice) })
      }
    }
  }
}

@Composable
@Preview
private fun LicensesPreview() = DiasTheme {
  Scaffold {
    Licenses(Modifier.padding(it), contentPadding = PaddingValues(Padding))
  }
}