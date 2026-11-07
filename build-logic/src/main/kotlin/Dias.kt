@file:JvmName("Dias")

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

/** The dynamic version catalog. */
internal val Project.libs
  get() = extensions.getByType<VersionCatalogsExtension>().find("libs").get()