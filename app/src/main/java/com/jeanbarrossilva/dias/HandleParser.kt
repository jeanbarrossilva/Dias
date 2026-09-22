package com.jeanbarrossilva.dias

import android.content.ComponentName
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import kotlin.reflect.KProperty1

object HandleParser {
  sealed class ParsingException: IllegalArgumentException {
    class UnparsableActivityInfo(
      name: String?,
      val missing: List<KProperty1<ActivityInfo, Any>>
    ): ParsingException(
      "Not enough information about the activity" +
        (name?.let { " $it" } ?: "") +
        ". The following are required for parsing one into a handler, but " +
        "were missing:\n" +
        missing.joinToString(separator = "\n") {
          "  • ${ActivityInfo::class.simpleName}.${it.name}"
        }
    )

    class NonexistentActivity
    internal constructor(cause: ClassNotFoundException):
      ParsingException(cause)

    class NonexistentPackage internal constructor(cause: Exception):
      ParsingException(cause)

    constructor(message: String): super(message)

    constructor(cause: Exception): super(cause)
  }

  @JvmStatic
  @Throws(ParsingException::class)
  fun parse(context: Context, activityInfo: ActivityInfo): Launcher.Handle {
    activityInfo.checkParsability()
    val packageName = activityInfo.packageName
    val targetContext =
        try {
          context.createPackageContext(
            packageName,
            Context.CONTEXT_IGNORE_SECURITY or Context.CONTEXT_INCLUDE_CODE
          )
        } catch (exception: PackageManager.NameNotFoundException) {
          throw ParsingException.NonexistentPackage(exception)
        }
    val targetPackageManager = targetContext.packageManager
    val label = activityInfo.loadLabel(targetPackageManager).toString()
    return Launcher.Handle(ComponentName(packageName, activityInfo.name), label)
  }

  @Throws(ParsingException.UnparsableActivityInfo::class)
  private fun ActivityInfo.checkParsability() {
    // ideally, we'd only instantiate the `ArrayList` once this info is deemed
    // unparsable, i.e., a required field's been left unset. on the other hand,
    // neither Java nor Kotlin appear to provide a way to do so that doesn't
    // involve allocating more memory (`lazy { … }` instantiates a wrapper!).
    val fields =
      ArrayList<KProperty1<ActivityInfo, Any>>(/* initialCapacity = */ 0)

    if (name == null)
      fields.add(ActivityInfo::name)
    if (labelRes == Resources.ID_NULL && nonLocalizedLabel == null)
      fields.addAll(
        arrayOf(ActivityInfo::labelRes, ActivityInfo::nonLocalizedLabel)
      )
    if (packageName == null)
      fields.add(ActivityInfo::packageName)
    if (fields.isNotEmpty())
      throw ParsingException.UnparsableActivityInfo(name, fields)
  }
}