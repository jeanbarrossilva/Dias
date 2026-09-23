package com.jeanbarrossilva.dias.app.home;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jeanbarrossilva.dias.core.IntHashSets;
import com.jeanbarrossilva.dias.Launcher;
import com.jeanbarrossilva.dias.databinding.HomeActivityBinding;

public class HomeActivity extends Activity {
  @Nullable
  private HomeActivityBinding binding;

  @Override
  public void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = HomeActivityBinding.inflate(getLayoutInflater());
    showHandles(binding);
    setContentView(binding.getRoot());
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    binding = null;
  }

  private void showHandles(@NonNull final HomeActivityBinding binding) {
    final Launcher launcher = new Launcher(this);
    launcher.pin(IntHashSets.of(0, 1));
    binding.pinnedHandlesView.setLauncher(launcher);
  }
}