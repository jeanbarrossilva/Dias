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

package com.jeanbarrossilva.dias.launcher;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.jeanbarrossilva.dias.launcher.Launcher.system;
import static com.jeanbarrossilva.dias.launcher.test.LauncherAssert.assertThat;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.iterable;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

import static java.util.UUID.randomUUID;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;

import com.jeanbarrossilva.OneTimeCopyOnWriteArrayList;
import com.jeanbarrossilva.dias.launcher.Launcher.SetupStrategy;
import com.jeanbarrossilva.dias.launcher.system.persistence.PinDatabase;
import com.jeanbarrossilva.dias.launcher.test.LauncherAssert;
import com.jeanbarrossilva.dias.launcher.test.SearchSessionAssert;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.UUID;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(Enclosed.class)
public final class LauncherTests {
  @RunWith(JUnitParamsRunner.class)
  public static final class SearchTests {
    @Test
    public void throwsWhenSearchingAfterSessionEnd() {
      final Launcher launcher = createLauncher();
      final SearchSessionAssert sessionAssert =
        assertThat(launcher).startSearchSession();
      final SearchSession session = sessionAssert.actual();
      launcher.endSearchSession(session);
      assertThatThrownBy(() -> sessionAssert.search(""))
        .isInstanceOf(SearchSession.EndedException.class)
        .extracting("id")
        .asInstanceOf(type(UUID.class))
        .isEqualTo(session.id);
      launcher.clear();
      launcher.close();
    }

    @Test
    public void returnsNullWhenQueryIsNull() {
      final Launcher launcher = createLauncher();
      assertThat(launcher).search(null).isNull();
      launcher.clear();
      launcher.close();
    }

    @Parameters({"", " "})
    @Test
    public void returnsViewToAllHandlesWhenQueryIsEmpty(final String query) {
      final Launcher launcher = createLauncher();
      assertThat(launcher)
        .search(query)
        .isInstanceOf(OneTimeCopyOnWriteArrayList.class)
        .containsExactlyElementsOf(launcher.getHandles());
      launcher.clear();
      launcher.close();
    }

    @Test
    public void searches() {
      final Launcher launcher = createLauncher();
      final LauncherAssert launcherAssert = assertThat(launcher);
      final ArrayList<Handle> expectedResults =
        new ArrayList<>(/* initialCapacity = */ 10);
      expectedResults.add(launcher.getHandles().getFirst());
      expectedResults.addAll(launcher.getHandles().stream().skip(9).toList());
      launcherAssert
        .handles()
        .hasSize(13)
        .extracting("label")
        .asInstanceOf(iterable(String.class))
        .allMatch(label -> label.startsWith("App "));
      launcherAssert
        .search("App 1")
        .containsExactlyElementsOf(expectedResults);
      launcher.clear();
      launcher.close();
    }

    @Test
    public void listensToSearches() {
      final Launcher launcher = createLauncher();
      assertThat(launcher)
        .startSearchSession()
        .doesNotHaveResults()
        .listenToSearches()
        .search("")
        .results()
        .isEqualTo(launcher.getHandles());
      launcher.clear();
      launcher.close();
    }

    @Test
    public void notifiesListenerOfEmptyResultsWhenSessionIsEnded() {
      final Launcher launcher = createLauncher();
      final SearchSessionAssert sessionAssert =
        assertThat(launcher).startSearchSession().listenToSearches();
      launcher.endSearchSession(sessionAssert.actual());
      sessionAssert.results().isEmpty();
      launcher.clear();
      launcher.close();
    }
  }

  @NonNull
  private static Launcher createLauncher() {
    final Context context = getApplicationContext();
    return system(
      context,
      Room.inMemoryDatabaseBuilder(context, PinDatabase.class)
        .allowMainThreadQueries()
        .build(),
      /* searchIndexName = */ "test-launcher-search-index-" + randomUUID(),
      SetupStrategy.sample()
    );
  }
}