package com.jeanbarrossilva.dias.launcher;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.jeanbarrossilva.dias.launcher.test.LauncherAssert.assertThat;

import static org.assertj.core.api.InstanceOfAssertFactories.iterable;

import com.jeanbarrossilva.OneTimeCopyOnWriteArrayList;
import com.jeanbarrossilva.dias.launcher.test.LauncherAssert;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(Enclosed.class)
public final class LauncherTests {
  @RunWith(JUnitParamsRunner.class)
  public static final class SearchTests {
    @Test
    public void returnsNullWhenQueryIsNull() {
      final Launcher launcher = Launcher.sample(getApplicationContext());
      assertThat(launcher).search(null).isNull();
    }

    @Parameters({"", " "})
    @Test
    public void returnsViewToAllHandlesWhenQueryIsEmpty(final String query) {
      final Launcher launcher = Launcher.sample(getApplicationContext());
      assertThat(launcher)
        .search(query)
        .isInstanceOf(OneTimeCopyOnWriteArrayList.class)
        .containsExactlyElementsOf(launcher.getHandles());
    }

    @Test
    public void searches() {
      final Launcher launcher = Launcher.sample(getApplicationContext());
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
  }
}