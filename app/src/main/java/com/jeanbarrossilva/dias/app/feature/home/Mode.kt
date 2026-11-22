package com.jeanbarrossilva.dias.app.feature.home

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.jeanbarrossilva.dias.launcher.Handle
import com.jeanbarrossilva.dias.launcher.Launcher
import com.jeanbarrossilva.dias.launcher.SearchSession
import kotlinx.parcelize.Parcelize
import java.io.Closeable
import java.util.Arrays.asList
import kotlin.collections.orEmpty

internal sealed interface Mode: Closeable, Parcelable {
  @Immutable
  @Parcelize
  object Pinned: Mode {
    override fun getHandles(launcher: Launcher): List<Handle> =
      @Suppress("ReplaceJavaStaticMethodWithKotlinAnalog")
      asList(*launcher.findPins())

    override fun close() = Unit
  }

  @Immutable
  data class Search(val query: String): Mode {
    private var launcher: Launcher? = null
    private var session: SearchSession? = null
    private var results = emptyList<Handle>()

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
      dest.writeParcelable(session, flags)
      dest.writeString(query)
      dest.writeParcelableList(results, flags)
    }

    override fun getHandles(launcher: Launcher): List<Handle> {
      val session = session
        ?: launcher
          .also { this.launcher = it }
          .startSearchSession()
          ?.also { this.session = it }
        ?: return launcher.handles
      return session.search(launcher.context, query).orEmpty().also {
        results = it
      }
    }

    override fun close() = launcher?.endSearchSession(session) ?: Unit

    companion object {
      @JvmField
      val CREATOR = object: Parcelable.Creator<Search> {
        override fun createFromParcel(source: Parcel?): Search? {
          return Search(query = source?.readString() ?: return null).apply {
            session = source
              .readParcelable(
                SearchSession::class.java.classLoader,
                SearchSession::class.java
              )
              ?: return null
          }
        }

        override fun newArray(size: Int) = arrayOfNulls<Search>(size)
      }
    }
  }

  fun getHandles(launcher: Launcher): List<Handle>
}