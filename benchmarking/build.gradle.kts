plugins {
  alias(libs.plugins.android.library)
}

android {
  namespace = "com.jeanbarrossilva.dias.internal.benchmarking"

  compileSdk {
    version = release(libs.versions.android.sdk.compile.get().toInt())
  }

  defaultConfig {
    testInstrumentationRunner =
      "androidx.benchmark.junit4.AndroidBenchmarkRunner"
    testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] =
      "DEBUGGABLE,NOT-AOT-COMPILED"
  }
}

configurations.all {
  exclude("org.agrona", "agrona")
}

dependencies {
  implementation(libs.android.benchmark)
  androidTestImplementation(project(":core"))
  androidTestImplementation(project(":core-test"))
}