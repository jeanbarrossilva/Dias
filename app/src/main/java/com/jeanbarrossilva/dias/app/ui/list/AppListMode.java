package com.jeanbarrossilva.dias.app.ui.list;

import static java.util.Arrays.asList;

import com.jeanbarrossilva.dias.launcher.Handle;
import com.jeanbarrossilva.dias.launcher.Launcher;
import com.jeanbarrossilva.dias.launcher.SearchSession;

import java.util.List;
import java.util.function.Consumer;

sealed abstract class AppListMode
  permits AppListMode.Pinned, AppListMode.Search {
  public static final class Pinned extends AppListMode {
    private final Launcher launcher;

    public Pinned(final Launcher launcher) {
      this.launcher = launcher;
    }

    @Override
    public List<Handle> getHandles() {
      return launcher == null ? null : asList(launcher.findPins());
    }

    @Override
    public void close() {}
  }

  public static final class Search extends AppListMode {
    private final SearchSession session;
    private List<Handle> results;

    public Search(
      final SearchSession session,
      final Consumer<List<Handle>> onSearchListener
    )
      throws SearchSession.EndedException {
      this.session = session;
      if (this.session == null)
        return;
      this.session.setOnSearchListener(results -> {
        this.results = results;
        onSearchListener.accept(results);
      });
      this.session.search("");
    }

    @Override
    public List<Handle> getHandles() {
      return results;
    }

    @Override
    public void close() {
      session.setOnSearchListener(null);
    }
  }

  public abstract List<Handle> getHandles();

  public abstract void close();
}