rootProject.name = "Dias"
include(":app")

pluginManagement.repositories {
  gradlePluginPortal()
  mavenCentral()

  google {
    content {
      includeGroupByRegex("com\\.android.*")
      includeGroupByRegex("com\\.google.*")
      includeGroupByRegex("androidx.*")
    }
  }
}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

  repositories {
    google()
    mavenCentral()
  }
}