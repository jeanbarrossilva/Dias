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

package com.jeanbarrossilva.dias.app.feature.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults.colors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.orcinus.orca.platform.focus.rememberImmediateFocusRequester
import com.jeanbarrossilva.dias.R
import com.jeanbarrossilva.dias.app.ui.ChangeEffect
import com.jeanbarrossilva.dias.app.ui.theme.DiasTheme
import com.jeanbarrossilva.dias.app.ui.theme.FloatingActionButton
import com.jeanbarrossilva.dias.launcher.Launcher

private const val ModeSwitchTransitionKey = 0

@Composable
fun Home(launcher: Launcher, modifier: Modifier = Modifier) {
  var mode by rememberSaveable { mutableStateOf<Mode>(Mode.Pinned) }
  Home(launcher, mode, onModeChange = { mode = it }, modifier)
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun Home(
  launcher: Launcher,
  mode: Mode,
  onModeChange: (Mode) -> Unit,
  modifier: Modifier = Modifier
) {
  val handles by rememberSaveable(launcher, mode) {
    mutableStateOf(mode.getHandles(launcher))
  }

  ChangeEffect(mode) { previous, current ->
    if (!current::class.isInstance(previous))
      previous.close()
  }

  BackHandler(enabled = mode is Mode.Search) {
    onModeChange(Mode.Pinned)
  }

  SharedTransitionLayout {
    Scaffold(
      modifier,
      floatingActionButton = {
        AnimatedContent(mode, label = "mode-switching") { mode ->
          when (mode) {
            is Mode.Pinned ->
              FloatingActionButton(
                onClick = { onModeChange(Mode.Search(query = "")) },
                Modifier.sharedBounds(
                  rememberSharedContentState(ModeSwitchTransitionKey),
                  this,
                  resizeMode =
                    SharedTransitionScope.ResizeMode.RemeasureToBounds
                )
              ) {
                Icon(
                  DiasTheme.Icons.Search,
                  contentDescription = stringResource(R.string.home_search)
                )
              }
            is Mode.Search ->
              BoxWithConstraints {
                TextField(
                  mode.query,
                  onValueChange = { onModeChange(mode.copy(query = it)) },
                  Modifier
                    .sharedBounds(
                      rememberSharedContentState(ModeSwitchTransitionKey),
                      this@AnimatedContent,
                      resizeMode =
                        SharedTransitionScope.ResizeMode.RemeasureToBounds
                    )
                    .padding(WindowInsets.ime.asPaddingValues())
                    .clip(FloatingActionButton.Shape)
                    .background(
                      FloatingActionButton.containerBrush,
                      FloatingActionButton.Shape
                    )
                    .padding(4.dp)
                    .width(maxWidth - FloatingActionButton.Margin * 2)
                    .focusRequester(rememberImmediateFocusRequester()),
                  label = { Text(stringResource(R.string.home_search_hint)) },
                  singleLine = true,
                  shape = FloatingActionButton.Shape,
                  colors = colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent
                  )
                )
              }
          }
        }
      }
    ) {
      LazyColumn(
        Modifier.imeNestedScroll().fillMaxSize(),
        contentPadding = PaddingValues(end = 24.dp) + it,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.End
      ) {
        itemsIndexed(handles, key = { _, handle -> handle.label }) {
          index, handle ->
          Text(
            handle.label,
            Modifier.animateItem().clickable(interactionSource = null) {
              launcher.context?.let(handle::launch)
            },
            textAlign = TextAlign.End,
            style = DiasTheme.Typography.bodyMediumEmphasized
          )

          Spacer(
            if (index < handles.lastIndex)
              Modifier.height(8.dp)
            else
              Modifier.imePadding()
          )
        }
      }
    }
  }
}

@Composable
@Preview
private fun HomeInPinnedModePreview() = DiasTheme {
  Home(Launcher.local(LocalContext.current), Mode.Pinned, onModeChange = {})
}

@Composable
@Preview
private fun HomeInSearchModePreview() = DiasTheme {
  Home(
    Launcher.local(LocalContext.current),
    Mode.Search(query = ""),
    onModeChange = {}
  )
}