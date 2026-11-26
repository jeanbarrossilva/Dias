/*
 * Copyright © 2026 Jean Silva
 *
 * Dias is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 2 of the License, or (at your option) any later
 * version.
 *
 * Dias is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Dias. If not, see https://www.gnu.org/licenses.
 */

package com.jeanbarrossilva.dias.launcher

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.VisibleForTesting
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.Objects.hash

/**
 * Holds information about a user-visible, installed application.
 *
 * @property id Identifier of this handle. Consists of this handle's associated
 *   activity's flattened name.
 * @property label Localized text considered to be the name of the application.
 */
@Parcelize
class Handle internal constructor(
  @get:JvmName("getID") val id: String,
  val label: String
): Comparable<Handle>, Parcelable {
  @IgnoredOnParcel
  private val intent =
    Intent().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).setComponent(name())

  constructor(parcel: Parcel): this(
    id = checkNotNull(parcel.readString()) { "id" },
    label = checkNotNull(parcel.readString()) { "" }
  )

  @VisibleForTesting
  internal constructor(
    packageName: String,
    activityName: String,
    label: String
  ): this(id = "$packageName/$activityName", label)

  override fun equals(other: Any?) =
    other is Handle && id == other.id && label == other.label

  override fun hashCode() = hash(id, label)
  override fun toString() = "$label ($id)"
  override fun compareTo(other: Handle) = label.compareTo(other.label)

  /** Instantiates the name of the activity associated to this handle. */
  @VisibleForTesting
  fun name() = ComponentName.unflattenFromString(id)

  /**
   * Starts the activity associated to this handle.
   *
   * @param context Context from which to start the activity.
   * @throws ActivityNotFoundException If this handle is associated to an
   *   activity nonexistent in the given context.
   */
  @Throws(ActivityNotFoundException::class)
  fun launch(context: Context) = context.startActivity(intent)

  companion object {
    /**
     * A handle for demonstration purposes.
     *
     * @see createSamples
     */
    @JvmStatic
    val sample = createSamples()[0]

    private const val QUERY_FLAGS = PackageManager.MATCH_ALL

    /** Instantiates handles for demonstration purposes.  */
    @JvmStatic
    fun createSamples() = Array(size = 12) {
      Handle(
        packageName = "com.jeanbarrossilva.dias",
        activityName = "com.jeanbarrossilva.dias.Activity${it + 1}",
        label = "App ${it + 1}"
      )
    }

    /**
     * Instantiates an intent for querying handles.
     *
     * @see queryAll
     */
    @JvmStatic
    fun createQueryIntent() =
      Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)

    @JvmStatic
    fun queryAll(packageManager: PackageManager, intent: Intent):
      MutableMap<String, Handle> {
      val resolveInfos =
        packageManager.queryIntentActivities(intent, QUERY_FLAGS)
      return HashMap<String, Handle>(resolveInfos.size).apply {
        queryAll(packageManager, resolveInfos, this)
      }
    }

    @JvmStatic
    fun queryAll(
      packageManager: PackageManager,
      intent: Intent,
      destination: MutableMap<String, Handle>
    ) = queryAll(
      packageManager,
      packageManager.queryIntentActivities(intent, QUERY_FLAGS),
      destination
    )

    @JvmStatic
    private fun queryAll(
      packageManager: PackageManager,
      resolveInfos: List<ResolveInfo>,
      destination: MutableMap<String, Handle>
    ) = resolveInfos
      .mapNotNull { parse(packageManager, it.activityInfo) }
      .forEach { destination[it.id] = it }

    @JvmStatic
    private fun parse(
      packageManager: PackageManager,
      activityInfo: ActivityInfo?
    ): Handle? {
      return Handle(
        activityInfo?.packageName ?: return null,
        activityInfo.name ?: return null,
        label = activityInfo.loadLabel(packageManager).toString()
      )
    }
  }
}