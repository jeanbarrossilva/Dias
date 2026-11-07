plugins {
  alias(libs.plugins.android.application)
  id("dias-android-module")
}

android {
  buildFeatures.viewBinding = true
  namespace = "com.jeanbarrossilva.dias"

  defaultConfig {
    applicationId = "com.jeanbarrossilva.dias"
    versionCode = 1
    versionName = "1.0"
  }
}

dependencies {
  implementation(project(":launcher"))
  implementation(libs.android.activity)
  implementation(libs.android.constraintLayout)
  implementation(libs.android.material)
  implementation(libs.android.room.runtime)
  testImplementation(libs.android.test.espresso.core)
  testImplementation(libs.assertJ)
  testImplementation(libs.jUnit4)
  testImplementation(libs.mockito)
  testImplementation(libs.robolectric)
}