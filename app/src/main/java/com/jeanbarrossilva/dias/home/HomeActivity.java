package com.jeanbarrossilva.dias.home;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jeanbarrossilva.dias.databinding.HomeActivityBinding;

import java.util.List;

public class HomeActivity extends Activity {
  @Nullable
  private HomeActivityBinding binding;

  @Override
  public void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = HomeActivityBinding.inflate(getLayoutInflater());
    showPinnedHandles(binding);
    setContentView(binding.getRoot());
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    binding = null;
  }

  private void showPinnedHandles(@NonNull final HomeActivityBinding binding) {
    final List<Handle> handles = Handle.queryAll(this).toList();
    final PinnedHandlesAdapter adapter =
      new PinnedHandlesAdapter(this, handles);
    binding.pinnedAppsView.setAdapter(adapter);
  }
}