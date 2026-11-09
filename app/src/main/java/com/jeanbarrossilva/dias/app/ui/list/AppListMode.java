package com.jeanbarrossilva.dias.app.ui.list;

import static java.util.Arrays.asList;

import com.jeanbarrossilva.dias.launcher.Handle;
import com.jeanbarrossilva.dias.launcher.Launcher;
import com.jeanbarrossilva.dias.launcher.SearchSession;

import java.util.List;

public sealed abstract class AppListMode
  permits AppListMode.Pinned, AppListMode.Search {
  public static final class Pinned extends AppListMode {
    private final Launcher launcher;

    public Pinned(final Launcher launcher) {
      this.launcher = launcher;
    }

    @Override
    List<Handle> getHandles() {
      return asList(launcher.findPins());
    }

    @Override
    void close() {}
  }

  public static final class Search extends AppListMode {
    private final SearchSession session;
    private List<Handle> results;

    public Search(final SearchSession session)
      throws SearchSession.EndedException {
      this.session = session;
      session.setOnSearchListener(results -> this.results = results);
      session.search("");
    }

    @Override
    List<Handle> getHandles() {
      return results;
    }

    @Override
    void close() {
      session.setOnSearchListener(null);
    }
  }

  abstract List<Handle> getHandles();

  abstract void close();
}