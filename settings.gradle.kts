rootProject.name = "Dias"

include(":app", ":launcher")
includeBuild("build-logic")

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

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

  repositories {
    google()
    mavenCentral()
  }
}
include(":launcher-test")
