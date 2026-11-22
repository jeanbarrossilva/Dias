@file:JvmName("FontFamilies")

package com.jeanbarrossilva.dias.app.ui

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontVariation.weight
import com.jeanbarrossilva.dias.R

private val spaceGrotesk = FontFamily(
  Font(
    R.font.space_grotesk,
    variationSettings = FontVariation.Settings(weight(400))
  )
);

/** Font family of the Space Grotesk typeface. */
internal val FontFamily.Companion.SpaceGrotesk
  get() = spaceGrotesk