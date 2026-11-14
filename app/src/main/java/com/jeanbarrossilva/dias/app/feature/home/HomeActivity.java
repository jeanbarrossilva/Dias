package com.jeanbarrossilva.dias.app.feature.home;

import static android.view.WindowInsets.Type.ime;
import static android.view.WindowInsets.Type.navigationBars;
import static android.view.WindowInsets.Type.statusBars;
import static android.view.WindowInsetsAnimation.Callback.DISPATCH_MODE_CONTINUE_ON_SUBTREE;

import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsAnimation;

import com.jeanbarrossilva.dias.app.ui.list.AppListView;
import com.jeanbarrossilva.dias.app.util.ObjectIntMaps;
import com.jeanbarrossilva.dias.app.util.SafeAreaInsetter;
import com.jeanbarrossilva.dias.app.util.SafeAreaInsetter.Transforming;
import com.jeanbarrossilva.dias.databinding.HomeActivityBinding;
import com.jeanbarrossilva.dias.launcher.Launcher;
import com.jeanbarrossilva.dias.launcher.SearchSession;

import static com.jeanbarrossilva.dias.app.Dias.requireLauncher;
import static com.jeanbarrossilva.dias.app.util.Insets.height;
import static com.jeanbarrossilva.dias.app.util.Views.setPaddingBottom;

import static java.util.Objects.requireNonNull;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class HomeActivity extends ComponentActivity {
  private final SafeAreaInsetter<?> safeAreaInsetter;
  private Searcher searcher;
  private SearchSession searchSession;
  private WindowInsetsAnimation.Callback appListPaddingUpdater;

  public HomeActivity() {
    safeAreaInsetter = SafeAreaInsetter.create(Transforming.translation());
  }

  @Override
  public void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    final HomeActivityBinding binding =
      HomeActivityBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    attachSafeAreaInsetter(binding);
    searcher = new Searcher(
                     binding.getRoot(),
      /* origin = */ binding.searchButton,
      /* target = */ binding.searchField
    );
    appListPaddingUpdater =
      new WindowInsetsAnimation.Callback(DISPATCH_MODE_CONTINUE_ON_SUBTREE) {
        @NonNull
        @Override
        public WindowInsets onProgress(
          @NonNull final WindowInsets windowInsets,
          @NonNull final List<WindowInsetsAnimation> runningAnimations
        ) {
          for (final WindowInsetsAnimation animation: runningAnimations) {
            if ((safeAreaInsetter.inset(windowInsets, animation) & ime()) == 0)
              continue;
            setPaddingBottom(
              binding.appListView,
              height(windowInsets.getInsets(ime()))
            );
            break;
          }
          return windowInsets;
        }
      };
    showAppList(binding);
    toggleSearchOnButtonClick(binding);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    safeAreaInsetter.detach();
    searcher.close();
    searcher = null;
  }

  private void showAppList(@NonNull final HomeActivityBinding binding) {
    final Launcher launcher = requireLauncher(this);
    launcher.pin(launcher.getHandles().getFirst().id);
    binding.appListView.switchToPinnedMode(launcher);
  }

  private void toggleSearchOnButtonClick(
    @NonNull final HomeActivityBinding binding
  ) {
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
        attachSafeAreaInsetter(binding);
        setPaddingBottom(appListView, 0);
      }
    });
    binding
      .searchButton
      .setOnClickListener(clickedButton -> searcher.open(this));
  }

  private void attachSafeAreaInsetter(
    @NonNull final HomeActivityBinding binding
  ) {
    safeAreaInsetter.attach(
                          binding.getRoot(),
      /* constraints = */ new ObjectIntMaps.Builder<View>(
                            /* initialCapacity = */ 3
                          )
                            .put(binding.appListView, statusBars())
                            .put(binding.searchButton, ime() | navigationBars())
                            .put(binding.searchField, ime() | navigationBars())
                            .build()
    );
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