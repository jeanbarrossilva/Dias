package com.jeanbarrossilva.dias.app.feature.home;

import static android.view.WindowInsets.Type.ime;
import static android.view.WindowInsets.Type.navigationBars;
import static android.view.WindowInsets.Type.statusBars;
import static android.view.WindowInsetsAnimation.Callback.DISPATCH_MODE_STOP;

import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsAnimation;

import com.jeanbarrossilva.dias.app.ui.list.AppListView;
import com.jeanbarrossilva.dias.app.util.SafeAreaConstraint;
import com.jeanbarrossilva.dias.databinding.HomeActivityBinding;
import com.jeanbarrossilva.dias.launcher.Launcher;
import com.jeanbarrossilva.dias.launcher.SearchSession;

import static com.jeanbarrossilva.dias.app.Dias.requireLauncher;
import static com.jeanbarrossilva.dias.app.util.Insets.height;
import static com.jeanbarrossilva.dias.app.util.Views.setPaddingBottom;

import static java.util.Objects.requireNonNull;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;

import java.util.List;

public class HomeActivity extends ComponentActivity {
  private HomeActivityBinding binding;
  private SafeAreaConstraint statusBarsConstraint;
  private SafeAreaConstraint bottomAreaConstraint;
  private Searcher searcher;
  private SearchSession searchSession;
  private final WindowInsetsAnimation.Callback appListPaddingUpdater;

  public HomeActivity() {
    super();
    this.appListPaddingUpdater =
      new WindowInsetsAnimation.Callback(DISPATCH_MODE_STOP) {
        @NonNull
        @Override
        public WindowInsets onProgress(
          @NonNull final WindowInsets windowInsets,
          @NonNull final List<WindowInsetsAnimation> runningAnimations
        ) {
          for (final WindowInsetsAnimation animation: runningAnimations)
            if (bottomAreaConstraint.offsetViews(windowInsets, animation))
              setPaddingBottom(
                binding.appListView,
                height(windowInsets.getInsets(ime()))
              );
          return windowInsets;
        }
      };
  }

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = HomeActivityBinding.inflate(getLayoutInflater());
    statusBarsConstraint = new SafeAreaConstraint(
      new View[]{binding.appListView},
      statusBars()
    );
    bottomAreaConstraint = new SafeAreaConstraint(
      new View[]{binding.searchButton, binding.searchField},
      ime() | navigationBars()
    );
    bottomAreaConstraint.attach(binding.getRoot());
    searcher = new Searcher(
                     binding.getRoot(),
      /* origin = */ binding.searchButton,
      /* target = */ binding.searchField
    );
    showAppList();
    toggleSearchOnButtonClick();
    setContentView(binding.getRoot());
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    binding = null;
    statusBarsConstraint.detach();
    statusBarsConstraint = null;
    bottomAreaConstraint.detach();
    bottomAreaConstraint = null;
    searcher.close();
    searcher = null;
  }

  private void showAppList() {
    final Launcher launcher = requireLauncher(this);
    launcher.pin(launcher.getHandles().getFirst().id);
    binding.appListView.switchToPinnedMode(launcher);
  }

  private void toggleSearchOnButtonClick() {
    searcher.setOnToggleListener(isSearching -> {
      final Launcher launcher = requireLauncher(this);
      final AppListView appListView = binding.appListView;
      if (isSearching) {
        searchSession = startAndRequireSearchSession();
        appListView.switchToSearchMode(searchSession);
        binding
          .appListView
          .setWindowInsetsAnimationCallback(appListPaddingUpdater);
      } else {
        launcher.endSearchSession(searchSession);
        appListView.switchToPinnedMode(launcher);
        appListView.setOnApplyWindowInsetsListener(bottomAreaConstraint);
        setPaddingBottom(appListView, 0);
      }
    });
    binding
      .searchButton
      .setOnClickListener(clickedButton -> searcher.open(this));
  }

  private SearchSession startAndRequireSearchSession()
    throws NullPointerException {
    @SuppressWarnings("resource")
    final SearchSession nullableResult  =
      requireLauncher(this).startSearchSession();

    return requireNonNull(
      nullableResult,
      "requireLauncher("
        + HomeActivity.class.getSimpleName()
        + ").startSearchSession()"
    );
  }
}