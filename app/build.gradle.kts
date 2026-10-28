plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.ksp)
}

android {
  buildFeatures.viewBinding = true
  namespace = "com.jeanbarrossilva.dias"
  testOptions.unitTests.isIncludeAndroidResources = true

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
    }
  }

  compileSdk {
    version = release(libs.versions.android.sdk.compile.get().toInt())
  }

  defaultConfig {
    applicationId = "com.jeanbarrossilva.dias"
    minSdk = compileSdk
    versionCode = 1
    versionName = "1.0"
  }

  compileOptions {
    val version = libs.versions
                      .java
                      .get()
                      .replaceFirst('.', '_')
                      .let { JavaVersion.valueOf("VERSION_$it") }
    sourceCompatibility = version
    targetCompatibility = version
  }

  testOptions.unitTests.isIncludeAndroidResources = true
}

dependencies {
  ksp(libs.android.room.compiler)
  implementation(libs.agrona)
  implementation(libs.android.activity)
  implementation(libs.android.constraintLayout)
  implementation(libs.android.material)
  implementation(libs.android.room.runtime)
  implementation(libs.guava)
  testImplementation(libs.android.espresso.intents)
  testImplementation(libs.android.test)
  testImplementation(libs.assertJ)
  testImplementation(libs.jUnit4)
  testImplementation(libs.mockito)
  testImplementation(libs.robolectric)
}