package com.jeanbarrossilva.dias.app.home;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.jeanbarrossilva.dias.Launcher;
import com.jeanbarrossilva.dias.R;
import com.jeanbarrossilva.dias.databinding.HomeActivityBinding;
import com.jeanbarrossilva.dias.persistence.PinDatabase;

import static androidx.room.Room.inMemoryDatabaseBuilder;

public class HomeActivity extends Activity {
  private Launcher launcher;
  private HomeActivityBinding binding;

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    launcher = new Launcher(
      this,
      inMemoryDatabaseBuilder(this, PinDatabase.class)
        .allowMainThreadQueries()
        .build()
    );
    launcher.pin(launcher.getHandles().getFirst().id);
    binding = HomeActivityBinding.inflate(getLayoutInflater());
    showHandles();
    setUpListExpansionToggle();
    setContentView(binding.getRoot());
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    launcher.close();
    binding = null;
  }

  private void showHandles() {
    binding.handleListView.setLauncher(launcher);
  }

  private void setUpListExpansionToggle() {
    updateListToggleButtonIcon();
    launcher.setOnPinningListener(
      pinning -> runOnUiThread(
        () -> binding
          .allAppsButton
          .setVisibility(launcher.hasPins() ? View.VISIBLE : View.GONE)
      )
    );
    binding.allAppsButton.setOnClickListener(button -> {
      binding.handleListView.toggleExpansion();
      updateListToggleButtonIcon();
    });
  }

  private void updateListToggleButtonIcon() {
    binding.allAppsButton.setIconResource(
      binding.handleListView.isExpanded()
        ? R.drawable.ic_collapse_content
        : R.drawable.ic_expand_content
    );
  }
}