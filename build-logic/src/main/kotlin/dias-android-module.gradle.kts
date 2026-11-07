@file:Suppress("NotShrinkingResources")

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion

/** The Android compile SDK version specified in the catalog. */
private val compileSdkVersion
  get() = libs
    .findVersion("android-sdk-compile")
    .get()
    .requiredVersion
    .toInt()

/** The version of Java specified in the catalog. */
private val javaVersion
  get() = libs
    .findVersion("java")
    .get()
    .requiredVersion
    .replaceFirst('.', '_')
    .let { JavaVersion.valueOf("VERSION_$it") }

extensions.configure(CommonExtension::class.java) {
  defaultConfig.apply {
    minSdk = compileSdkVersion
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes.named("release") {
    isMinifyEnabled = true
  }

  compileSdk {
    version = release(compileSdkVersion)
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