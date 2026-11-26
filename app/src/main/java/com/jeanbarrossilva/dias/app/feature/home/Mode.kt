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

package com.jeanbarrossilva.dias.app.feature.home

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.github.difflib.DiffUtils.diff
import com.jeanbarrossilva.dias.launcher.Handle
import com.jeanbarrossilva.dias.launcher.Launcher
import com.jeanbarrossilva.dias.launcher.SearchSession
import kotlinx.parcelize.Parcelize
import java.io.Closeable
import java.util.Arrays.asList
import kotlin.collections.orEmpty

@Stable
internal sealed class Mode: Closeable, Parcelable {
  @Immutable
  @Parcelize
  object Pinned: Mode() {
    override fun getHandles(launcher: Launcher): List<Handle> =
      @Suppress("ReplaceJavaStaticMethodWithKotlinAnalog")
      asList(*launcher.findPins())

    override fun close() = Unit
  }

  class Search private constructor(
    private var launcherState: MutableState<Launcher?>,
    private var session: SearchSession,
    private var queryState: MutableState<String>,
    private var lastHandles: List<Handle>?
  ): Mode() {
    var query
      get() = queryState.value
      set(query) {
        queryState.value = query
        launcher?.let(::getHandles)
      }

    private var launcher by launcherState
    private val handles = mutableStateListOf<Handle>()

    constructor(launcher: Launcher): this(
      mutableStateOf(launcher),
      launcher.startSearchSession(),
      queryState = mutableStateOf(""),
      lastHandles = emptyList()
    )

    override fun describeContents() = 0

    override fun getHandles(launcher: Launcher): List<Handle> {
      this.launcher = launcher
      val results = session.search(launcher.context, query).orEmpty()
      diff(handles, results)!!.applyToExisting(handles)
      lastHandles = handles
      return handles
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
      dest.writeParcelable(session, flags)
      dest.writeString(query)
      dest.writeParcelableList(lastHandles, flags)
    }

    override fun close() {
      launcher?.endSearchSession(session)
      launcher = null
      queryState.value = ""
      lastHandles = null
    }

    companion object {
      @JvmField
      val CREATOR = object: Parcelable.Creator<Search> {
        override fun createFromParcel(source: Parcel?): Search? {
          return Search(
            launcherState = mutableStateOf(null),
            session = source
              ?.readParcelable(
                SearchSession::class.java.classLoader,
                SearchSession::class.java
              )
              ?: return null,
            queryState = mutableStateOf(source.readString() ?: return null),
            lastHandles =
              source.createTypedArrayList(Handle.CREATOR) ?: return null
          )
        }

        override fun newArray(size: Int) = arrayOfNulls<Search>(size)
      }
    }
  }

  abstract fun getHandles(launcher: Launcher): List<Handle>
  override fun equals(other: Any?) = this::class.isInstance(other)
  override fun hashCode() = this::class.hashCode()
}