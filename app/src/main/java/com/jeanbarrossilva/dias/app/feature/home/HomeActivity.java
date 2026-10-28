package com.jeanbarrossilva.dias.app.feature.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.jeanbarrossilva.dias.app.feature.drawer.DrawerActivity;
import com.jeanbarrossilva.dias.databinding.HomeActivityBinding;
import com.jeanbarrossilva.dias.launcher.Launcher;

import static com.jeanbarrossilva.dias.app.Dias.requireLauncher;
import static com.jeanbarrossilva.dias.core.Objects.either;

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
    binding.handleListView.setLauncher(launcher);
  }

  private void setUpNavigationToDrawer() {
    binding.allAppsButton.setOnClickListener(allAppsButton -> {
      final Context context = either(allAppsButton.getContext(), this);
      if (context == null)
        return;
      final Intent intent =
        new Intent().setClass(context, DrawerActivity.class);
      context.startActivity(intent);
    });
  }
}