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

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults.exitUntilCollapsedScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import br.com.orcinus.orca.platform.autos.kit.action.setting.Section
import com.jeanbarrossilva.dias.R
import com.jeanbarrossilva.dias.app.feature.settings.SettingsDefaults.Padding
import com.jeanbarrossilva.dias.app.ui.spacing
import com.jeanbarrossilva.dias.app.ui.theme.DiasTheme
import kotlinx.serialization.Serializable

private enum class ContentType {
  Authorship,
  Section
}

private object Routes {
  @Serializable
  object Licenses

  @Serializable
  object Settings
}

@Composable
internal fun Settings(
  onNavigationToPrivacyPolicy: () -> Unit,
  onExit: () -> Unit,
  modifier: Modifier = Modifier
) {
  val navController = rememberNavController()
  val backStackEntry by navController.currentBackStackEntryAsState()
  val topAppBarScrollBehavior = exitUntilCollapsedScrollBehavior()

  Scaffold(
    modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
    topBar = {
      LargeFlexibleTopAppBar(
        title = {
          Text(
            backStackEntry?.destination?.label?.toString()
              ?: stringResource(R.string.settings)
          )
        },
        navigationIcon = {
          IconButton(onClick = {
            if (!navController.popBackStack())
              onExit()
          }) {
            Icon(
              DiasTheme.Icons.Return,
              contentDescription = stringResource(R.string.app_navigation_pop)
            )
          }
        },
        scrollBehavior = topAppBarScrollBehavior
      )
    }
  ) { scaffoldPadding ->
    val contentPadding = remember(scaffoldPadding) {
      PaddingValues(Padding) + scaffoldPadding
    }

    NavHost(
      navController,
      startDestination = Routes.Settings
    ) {
      composable<Routes.Licenses> { backStackEntry ->
        LaunchedEffect(Unit) {
          backStackEntry.destination.label = ""
          topAppBarScrollBehavior.state.apply {
            heightOffset = heightOffsetLimit
          }
        }

        Licenses(contentPadding = contentPadding)
      }

      composable<Routes.Settings> { backStackEntry ->
        val label = stringResource(R.string.settings)

        LaunchedEffect(Unit) {
          backStackEntry.destination.label = label
        }

        LazyColumn(
          Modifier.padding(
            start = contentPadding
              .calculateStartPadding(LocalLayoutDirection.current),
            end = contentPadding
              .calculateEndPadding(LocalLayoutDirection.current)
          )
        ) {
          spacing(Orientation.Vertical, contentPadding.calculateTopPadding())

          item(contentType = ContentType.Section) {
            LegalSection(
              onNavigationToLicenses = {
                navController.navigate(Routes.Licenses)
              },
              onNavigationToPrivacyPolicy
            )
          }

          spacing(Orientation.Vertical, DiasTheme.Spacing.xl)
          item(contentType = ContentType.Authorship) { Authorship() }
          spacing(Orientation.Vertical, contentPadding.calculateBottomPadding())
        }
      }
    }
  }
}

@Composable
private fun LegalSection(
  onNavigationToLicenses: () -> Unit,
  onNavigationToPrivacyPolicy: () -> Unit,
  modifier: Modifier = Modifier
) {
  Section(stringResource(R.string.settings_legal), modifier) {
    setting(
      label = { Text(stringResource(R.string.settings_legal_licenses)) },
      onClick = onNavigationToLicenses
    ) {
      icon(contentDescription = {
        stringResource(R.string.settings_navigation_to_licenses)
      }) {
        ArrowForward
      }
    }
  }
}

@Composable
private fun Authorship(modifier: Modifier = Modifier) {
  val context = LocalContext.current
  val versionName = remember(context) {
    context
      .packageManager
      .getPackageInfo(context.packageName, /* flags = */ 0)
      .versionName
      ?: "v0.0"
  }

  Column(modifier.fillMaxWidth()) {
    CompositionLocalProvider(
      LocalContentColor provides LocalContentColor.current.copy(alpha = .5f)
    ) {
      ProvideTextStyle(
        DiasTheme.Typography.labelMedium.copy(LocalContentColor.current)
      ) {
        Text("${stringResource(R.string.app_name)}, $versionName")
        Text(stringResource(R.string.settings_authorship))
        Text(buildAnnotatedString {
          append(
            stringResource(
              R.string.settings_authorship_source_code_availability_prefix
            )
              + ' '
          )
          withLink(
            LinkAnnotation.Url("https://github.com/jeanbarrossilva/Dias")
          ) {
            append("GitHub")
          }
        })
      }
    }
  }
}

@Composable
@Preview
private fun SettingsPreview() = DiasTheme {
  Settings(onNavigationToPrivacyPolicy = {}, onExit = {})
}