package com.jeanbarrossilva.dias.app.feature.home;

import static android.view.WindowInsets.Type.ime;
import static android.view.WindowInsets.Type.navigationBars;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.jeanbarrossilva.dias.app.ui.list.AppListMode;
import com.jeanbarrossilva.dias.app.ui.list.AppListView;
import com.jeanbarrossilva.dias.app.util.SafeAreaPusher;
import com.jeanbarrossilva.dias.databinding.HomeActivityBinding;
import com.jeanbarrossilva.dias.launcher.Launcher;
import com.jeanbarrossilva.dias.launcher.SearchSession;

import static com.jeanbarrossilva.dias.app.Dias.requireLauncher;

import androidx.activity.ComponentActivity;

public class HomeActivity extends ComponentActivity {
  private HomeActivityBinding binding;
  private Searcher searcher;
  private SafeAreaPusher bottomPusher;

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = HomeActivityBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    searcher = new Searcher(
      this,
      binding.getRoot(),
      binding.searchButton,
      binding.searchField
    );
    bottomPusher = new SafeAreaPusher(
      new View[]{binding.searchButton, binding.searchField},
      ime() | navigationBars()
    );
    bottomPusher.attach(binding.getRoot());
    showAppList();
    setUpSearch();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    binding = null;
    searcher.close();
    searcher = null;
    bottomPusher.detach();
    bottomPusher = null;
  }

  private void showAppList() {
    final Launcher launcher = requireLauncher(this);
    final AppListView view = binding.appListView;
    launcher.pin(launcher.getHandles().getFirst().id);
    view.setMode(new AppListMode.Pinned(launcher));
  }

  @SuppressWarnings({"DataFlowIssue", "resource"})
  private void setUpSearch() {
    final AppListView appListView = binding.appListView;
    final ImageButton button = binding.searchButton;
    button.setOnClickListener(clickedButton -> {
      final SearchSession searchSession =
        requireLauncher(this).startSearchSession();
      appListView.setMode(new AppListMode.Search(searchSession));
      searcher.setOnCancellationListener(
        () -> appListView.setMode(new AppListMode.Pinned(requireLauncher(this)))
      );
      searcher.open();
    });
  }
}