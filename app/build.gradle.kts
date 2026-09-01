plugins {
  alias(libs.plugins.android.application)
}

android {
  namespace = "com.jeanbarrossilva.dias"

  compileSdk {
    version = release(36) {
      minorApiLevel = 1
    }
  }

  defaultConfig {
    applicationId = "com.jeanbarrossilva.dias"
    minSdk = 36
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
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
}