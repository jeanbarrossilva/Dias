plugins {
  alias(libs.plugins.android.application)
}

android {
  buildFeatures.viewBinding = true
  namespace = "com.jeanbarrossilva.dias"
  testOptions.unitTests.isIncludeAndroidResources = true

  compileSdk {
    version = release(libs.versions.android.sdk.compile.get().toInt())
  }

  defaultConfig {
    applicationId = "com.jeanbarrossilva.dias"
    minSdk = compileSdk
    versionCode = 1
    versionName = "1.0"
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
    }
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
}

dependencies {
  implementation(project(":core"))
  implementation(libs.android.activity)
  implementation(libs.android.constraintLayout)
  implementation(libs.android.material)
  testImplementation(libs.android.espresso.intents)
  testImplementation(libs.android.test)
  testImplementation(libs.jUnit4)
  testImplementation(libs.robolectric)
}