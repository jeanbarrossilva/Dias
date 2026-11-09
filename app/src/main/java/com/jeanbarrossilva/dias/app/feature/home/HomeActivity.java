package com.jeanbarrossilva.dias.app.feature.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import com.jeanbarrossilva.dias.app.ui.list.AppListMode;
import com.jeanbarrossilva.dias.app.util.SafeAreaListener;
import com.jeanbarrossilva.dias.app.feature.drawer.DrawerActivity;
import com.jeanbarrossilva.dias.databinding.HomeActivityBinding;
import com.jeanbarrossilva.dias.launcher.Launcher;

import static com.jeanbarrossilva.dias.app.Dias.requireLauncher;
import static com.jeanbarrossilva.dias.app.util.Objects.either;

public class HomeActivity extends Activity {
  private HomeActivityBinding binding;

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    final Launcher launcher = requireLauncher(this);
    binding = HomeActivityBinding.inflate(getLayoutInflater());
    showAppList(launcher);
    setUpNavigationToDrawer();
    setContentView(binding.getRoot());
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    binding = null;
  }

  private void showAppList(final Launcher launcher) {
    launcher.pin(launcher.getHandles().getFirst().id);
    binding.handleListView.setMode(new AppListMode.Pinned(launcher));
  }

  private void setUpNavigationToDrawer() {
    final ImageButton button = binding.searchButton;
    button.setOnApplyWindowInsetsListener(SafeAreaListener.navigationBars);
    button.setOnClickListener(clickedButton -> {
      final Context context = either(clickedButton.getContext(), this);
      final Intent intent =
        new Intent().setClass(context, DrawerActivity.class);
      context.startActivity(intent);
    });
  }
}