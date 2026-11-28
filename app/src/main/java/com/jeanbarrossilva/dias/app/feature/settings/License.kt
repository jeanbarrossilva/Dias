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

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink

private const val APACHE_2_URL = "https://www.apache.org/licenses/LICENSE-2.0"

internal enum class License(
  val title: String,
  val notices: List<AnnotatedString>
) {
  AGRONA(
    title = "Agrona",
    notices = listOf(generateApache2Notice(2014..2025, "Real Logic Unlimited"))
  ),
  ASSERTJ(
    title = "AssertJ",
    notices = listOf(buildAnnotatedString {
      appendLine("AssertJ")
      appendLine("Copyright 2012–2025 the original author or authors.")
      appendLine()
      appendLine("This product includes software developed at")
      appendLine(
        "- https://code.google.com/archive/p/java-diff-utils/ (project under " +
          "the Apache License 2.0)"
      )
      appendLine(
        "- https://github.com/jdereg/java-util - the DeepEquals feature " +
          "inspired our recursive comparison (project under Apache License 2.0)"
      )
    })
  ),
  GUAVA(
    title = "Guava",
    notices = listOf(generateApache2Notice(2_009..2026, "Google LLC"))
  ),
  JAVA_DIFF_UTILS(
    title = "java-diff-utils",
    notices = listOf(generateApache2Notice(2009..2026, "Tobias Warneke"))
  )
}

private fun generateApache2Notice(years: IntRange, holder: String) =
  buildAnnotatedString {
    appendLine("Copyright © ${years.first}–${years.last} $holder")
    appendLine()
    appendLine(
      "Licensed under the Apache License, Version 2.0 (the \"License\"); " +
        "you may not use this file except in compliance with the " +
        "License. You may obtain a copy of the License at"
    )
    appendLine()
    withLink(LinkAnnotation.Url(APACHE_2_URL)) {
      appendLine(APACHE_2_URL)
    }
    appendLine()
    appendLine(
      "Unless required by applicable law or agreed to in writing, " +
        "software distributed under the License is distributed on an " +
        "\"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, " +
        "either express or implied. See the License for the specific " +
        "language governing permissions and limitations under the License."
    )
  }