package com.jeanbarrossilva.dias.launcher.test;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.ObjectAssert;
import org.springframework.test.util.MethodAssert;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static com.jeanbarrossilva.dias.launcher.test.util.InstanceOfAssertFactories.SEARCH_SESSION;
import static com.jeanbarrossilva.dias.launcher.test.util.InstanceOfAssertFactories.listFromArray;
import static org.assertj.core.api.InstanceOfAssertFactories.BOOLEAN;
import static org.assertj.core.api.InstanceOfAssertFactories.list;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jeanbarrossilva.dias.launcher.Handle;
import com.jeanbarrossilva.dias.launcher.Launcher;
import com.jeanbarrossilva.dias.launcher.SearchSession;

/// Asserter for a [Launcher].
///
/// @see #assertThat(Launcher)
public final class LauncherAssert
  extends AbstractAssert<LauncherAssert, Launcher> {
  private LauncherAssert(final Launcher launcher) {
    super(launcher, LauncherAssert.class);
  }

  /// Returns an asserter for the launcher's handles.
  ///
  /// @see Launcher#getHandles()
  /// @throws AssertionError If the launcher is `null`.
  @NonNull
  public ListAssert<Handle> handles() {
    try {
      isNotNull();
      return MethodAssert.assertThat(Launcher.class, "getHandles")
                         .invoke(actual())
                         .asInstanceOf(list(Handle.class));
    } catch (final IllegalAccessException
                 | InvocationTargetException
                 | NoSuchMethodException cause) {
      throw new RuntimeException(cause);
    }
  }

  /// Asserts that the launcher has at least one handle.
  ///
  /// @see Launcher#hasHandles()
  @NonNull
  public LauncherAssert hasHandles() {
    assertThatHasHandles().isTrue();
    return this;
  }

  /// Asserts that calling [Launcher#hasPins()] on the launcher returns `true`.
  @NonNull
  public LauncherAssert hasPins() {
    createHasPinsAssert().isTrue();
    return this;
  }

  /// Asserts that calling [Launcher#hasPins()] on the launcher returns `false`.
  @NonNull
  public LauncherAssert doesNotHavePins() {
    createHasPinsAssert().isFalse();
    return this;
  }

  /// Returns an asserter for the launcher's pins.
  ///
  /// @see Launcher#findPins()
  /// @throws AssertionError If the launcher is `null`.
  @NonNull
  public ListAssert<Handle> pins() throws AssertionError {
    try {
      isNotNull();
      return MethodAssert.assertThat(Launcher.class, "findPins")
                         .invokeAsync(actual())
                         .asInstanceOf(listFromArray(Handle[].class));
    } catch (final IllegalAccessException
                 | InvocationTargetException
                 | NoSuchMethodException cause) {
      throw new RuntimeException(cause);
    }
  }

  /// Asserts that the handle identified by the given ID is currently pinned.
  ///
  /// @param id ID of the handle.
  /// @see Launcher#isPinned(String)
  @NonNull
  public LauncherAssert hasPinned(final String id) throws AssertionError {
    createIsPinnedAssert(id).isTrue();
    return this;
  }

  /// Asserts that the handle identified by the given ID is currently unpinned.
  ///
  /// @param id ID of the handle.
  /// @see Launcher#isPinned(String)
  @NonNull
  public LauncherAssert hasUnpinned(final String id) throws AssertionError {
    createIsPinnedAssert(id).isFalse();
    return this;
  }

  /// Returns an asserter for the handles found in the launcher by the search
  /// with the given query.
  ///
  /// @param query Text to search handles by.
  /// @throws AssertionError If the launcher is `null`.
  @NonNull
  public ListAssert<Handle> search(@Nullable final String query)
    throws AssertionError {
    final SearchSession session = startSearchSession().actual();
    final ListAssert<Handle> castResult;
    try {
      final ObjectAssert<Object> uncastResult =
        MethodAssert.assertThat(SearchSession.class, "search", String.class)
                    .invoke(session, query);
      castResult = uncastResult.actual() == null
        ? new ListAssert<>((List<Handle>) null)
        : uncastResult.asInstanceOf(list(Handle.class));
    } catch (final IllegalAccessException
                 | InvocationTargetException
                 | NoSuchMethodException cause) {
      throw new RuntimeException(cause);
    } finally {
      actual.endSearchSession(session);
    }
    return castResult;
  }

  /// Starts a search session in the launcher.
  ///
  /// This method allows for more control over the started session (e.g., adding
  /// and removing a listener), imposing the responsibility of ending it to the
  /// caller. In case this level of granularity is unnecessary for your use
  /// case, refer to [#search(String)] instead.
  ///
  /// @return An asserter for the started search session.
  /// @see Launcher#startSearchSession()
  /// @throws AssertionError If the launcher is `null`.
  @NonNull
  public SearchSessionAssert startSearchSession()
    throws AssertionError {
    try {
      return MethodAssert.assertThat(Launcher.class, "startSearchSession")
                         .invoke(isNotNull().actual())
                         .asInstanceOf(SEARCH_SESSION);
    } catch (final IllegalAccessException
                 | InvocationTargetException
                 | NoSuchMethodException cause) {
      throw new RuntimeException(cause);
    }
  }

  /// Instantiates an asserter for the given launcher.
  ///
  /// @param actual Launcher for which the asserter is.
  /// @return The instantiated asserter.
  @NonNull
  public static LauncherAssert assertThat(@Nullable final Launcher actual) {
    return new LauncherAssert(actual);
  }

  private AbstractBooleanAssert<?> assertThatHasHandles()
    throws AssertionError {
    try {
      return MethodAssert.assertThat(Launcher.class, "hasHandles")
                         .invoke(isNotNull().actual())
                         .asInstanceOf(BOOLEAN);
    } catch (final IllegalAccessException
                 | InvocationTargetException
                 | NoSuchMethodException cause) {
      throw new RuntimeException(cause);
    }
  }

  private AbstractBooleanAssert<?> createHasPinsAssert() throws AssertionError {
    try {
      return MethodAssert.assertThat(Launcher.class, "hasPins")
                         .invokeAsync(isNotNull().actual())
                         .asInstanceOf(BOOLEAN);
    } catch (final IllegalAccessException
                 | InvocationTargetException
                 | NoSuchMethodException cause) {
      throw new RuntimeException(cause);
    }
  }

  private AbstractBooleanAssert<?> createIsPinnedAssert(final String id)
    throws AssertionError {
    try {
      return MethodAssert.assertThat(Launcher.class, "isPinned", String.class)
                         .invokeAsync(isNotNull().actual(), id)
                         .asInstanceOf(BOOLEAN);
    } catch (final IllegalAccessException
                 | InvocationTargetException
                 | NoSuchMethodException cause) {
      throw new RuntimeException(cause);
    }
  }
}