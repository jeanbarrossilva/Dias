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

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.android.compose)
  alias(libs.plugins.kotlin.parcelize)
  alias(libs.plugins.kotlin.serialization)
  id("dias-android-module")
}

android {
  buildFeatures.compose = true
  namespace = "com.jeanbarrossilva.dias"

  defaultConfig {
    applicationId = "com.jeanbarrossilva.dias"
    versionCode = 1
    versionName = "v1.0"
  }
}

dependencies {
  implementation(project(":launcher"))
  implementation(libs.android.activity.compose)
  implementation(libs.android.compose.animation.core)
  implementation(libs.android.compose.material.icons)
  implementation(libs.android.compose.material3)
  implementation(libs.android.compose.ui.tooling)
  implementation(libs.android.constraintLayout)
  implementation(libs.android.material)
  implementation(libs.android.navigation.compose)
  implementation(libs.android.room.runtime)
  implementation(libs.javaDiffUtils)
  implementation(libs.kotlin.reflect)
  testImplementation(libs.android.test.espresso.core)
  testImplementation(libs.assertJ)
  testImplementation(libs.jUnit4)
  testImplementation(libs.mockito)
  testImplementation(libs.robolectric)
}