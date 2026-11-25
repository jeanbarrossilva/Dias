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

@file:Suppress("NotShrinkingResources")

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion

private val majorCompileSdkVersion
  get() = getSdkVersion("compile-major")

private val javaVersion
  get() = libs
    .findVersion("java")
    .get()
    .requiredVersion
    .replaceFirst('.', '_')
    .let { JavaVersion.valueOf("VERSION_$it") }

private fun getSdkVersion(suffix: String) = libs
  .findVersion("android-sdk-$suffix")
  .get()
  .requiredVersion
  .toInt()

extensions.configure(CommonExtension::class.java) {
  defaultConfig.apply {
    minSdk = getSdkVersion("min")
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes.named("release") {
    isMinifyEnabled = true
  }

  compileSdk {
    version = release(majorCompileSdkVersion) {
      minorApiLevel = getSdkVersion("compile-minor")
    }
  }

  testOptions.unitTests.isIncludeAndroidResources = true
}

pluginManager.withPlugin("com.android.application") {
  extensions.configure(ApplicationExtension::class.java) {
    buildTypes.named("release") {
      isShrinkResources = true
    }

    compileOptions {
      sourceCompatibility = javaVersion
      targetCompatibility = javaVersion
    }
  }
}

pluginManager.withPlugin("com.android.library") {
  extensions.configure(LibraryExtension::class.java) {
    compileOptions {
      sourceCompatibility = javaVersion
      targetCompatibility = javaVersion
    }
  }
}