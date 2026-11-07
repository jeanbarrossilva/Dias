package com.jeanbarrossilva.dias.launcher.test;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.ObjectAssert;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.test.util.MethodAssert;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static com.jeanbarrossilva.dias.launcher.test.util.InstanceOfAssertFactories.listFromArray;
import static org.assertj.core.api.InstanceOfAssertFactories.BOOLEAN;
import static org.assertj.core.api.InstanceOfAssertFactories.list;

import com.jeanbarrossilva.dias.launcher.Handle;
import com.jeanbarrossilva.dias.launcher.Launcher;
import com.jeanbarrossilva.dias.launcher.SearchSession;

/// Asserter for a [Launcher].
///
/// @see #assertThat(Launcher)
public final class LauncherAssert
  extends AbstractAssert<LauncherAssert, Launcher> {
  private LauncherAssert(@NotNull final Launcher launcher) {
    super(launcher, LauncherAssert.class);
  }

  /// Returns an asserter for the launcher's handles.
  ///
  /// @see Launcher#getHandles()
  @NotNull
  public ListAssert<Handle> handles() {
    try {
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
  @NotNull
  public LauncherAssert hasHandles() {
    assertThatHasHandles().isTrue();
    return this;
  }

  /// Asserts that calling [Launcher#hasPins()] on the launcher returns `true`.
  @NotNull
  public LauncherAssert hasPins() {
    createHasPinsAssert().isTrue();
    return this;
  }

  /// Asserts that calling [Launcher#hasPins()] on the launcher returns `false`.
  @NotNull
  public LauncherAssert doesNotHavePins() {
    createHasPinsAssert().isFalse();
    return this;
  }

  /// Returns an asserter for the launcher's pins.
  ///
  /// @see Launcher#findPins()
  @NotNull
  public ListAssert<Handle> pins() {
    try {
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
  @NotNull
  public LauncherAssert hasPinned(final String id) {
    createIsPinnedAssert(id).isTrue();
    return this;
  }

  /// Asserts that the handle identified by the given ID is currently unpinned.
  ///
  /// @param id ID of the handle.
  /// @see Launcher#isPinned(String)
  @NotNull
  public LauncherAssert hasUnpinned(final String id) {
    createIsPinnedAssert(id).isFalse();
    return this;
  }

  /// Returns an asserter for the handles found in the launcher by the search
  /// with the given query.
  ///
  /// @param query Text to search handles by.
  @NotNull
  public ListAssert<Handle> search(@Nullable final String query) {
    final SearchSession session = actual.startSearchSession();
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

  /// Instantiates an asserter for the given launcher.
  ///
  /// @param launcher Launcher for which the asserter is.
  /// @return The instantiated asserter, or `null` if the launcher is `null`.
  @Nullable
  public static LauncherAssert assertThat(@Nullable final Launcher launcher) {
    if (launcher == null)
      return null;
    return new LauncherAssert(launcher);
  }

  private AbstractBooleanAssert<?> assertThatHasHandles() {
    try {
      return MethodAssert.assertThat(Launcher.class, "hasHandles")
                         .invoke(actual())
                         .asInstanceOf(BOOLEAN);
    } catch (final IllegalAccessException
                 | InvocationTargetException
                 | NoSuchMethodException cause) {
      throw new RuntimeException(cause);
    }
  }

  private AbstractBooleanAssert<?> createHasPinsAssert() {
    try {
      return MethodAssert.assertThat(Launcher.class, "hasPins")
                         .invokeAsync(actual())
                         .asInstanceOf(BOOLEAN);
    } catch (final IllegalAccessException
                 | InvocationTargetException
                 | NoSuchMethodException cause) {
      throw new RuntimeException(cause);
    }
  }

  private AbstractBooleanAssert<?> createIsPinnedAssert(final String id) {
    try {
      return MethodAssert.assertThat(Launcher.class, "isPinned", String.class)
                         .invokeAsync(actual(), id)
                         .asInstanceOf(BOOLEAN);
    } catch (final IllegalAccessException
                 | InvocationTargetException
                 | NoSuchMethodException cause) {
      throw new RuntimeException(cause);
    }
  }
}