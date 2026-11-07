plugins {
  alias(libs.plugins.android.library)
  id("dias-android-module")
}

android.namespace = "com.jeanbarrossilva.dias.launcher.test"

dependencies {
  api(libs.android.test.espresso.intents)
  api(libs.assertJ)

  implementation(project(":launcher"))
  implementation(libs.jUnit4)
}