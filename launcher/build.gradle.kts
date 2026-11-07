plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.ksp)
  id("dias-android-module")
}

android {
  namespace = "com.jeanbarrossilva.dias.launcher"
}

dependencies {
  androidTestImplementation(project(":launcher-test"))
  androidTestImplementation(libs.android.test)
  androidTestImplementation(libs.jUnit4)
  androidTestImplementation(libs.jUnitParams)

  annotationProcessor(libs.android.appSearch.compiler)

  api(libs.agrona)

  ksp(libs.android.room.compiler)

  implementation(libs.android.appSearch)
  implementation(libs.android.appSearch.localStorage)
  implementation(libs.android.collection)
  implementation(libs.android.room.runtime)
  implementation(libs.guava)

  testImplementation(project(":launcher-test"))
  testImplementation(libs.android.test)
  testImplementation(libs.jUnit4)
  testImplementation(libs.mockito)
  testImplementation(libs.robolectric)
}