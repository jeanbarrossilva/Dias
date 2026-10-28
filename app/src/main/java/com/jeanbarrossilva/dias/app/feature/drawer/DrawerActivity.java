package com.jeanbarrossilva.dias.app.feature.drawer;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.jeanbarrossilva.dias.app.Dias;
import com.jeanbarrossilva.dias.databinding.DrawerActivityBinding;
import com.jeanbarrossilva.dias.launcher.Handle;
import com.jeanbarrossilva.dias.launcher.Launcher;
import com.jeanbarrossilva.dias.launcher.SearchSession;

import java.util.List;

import kotlin.Unit;

import static android.view.WindowInsets.Type.ime;
import static android.view.WindowInsets.Type.navigationBars;
import static android.view.WindowInsets.Type.statusBars;
import static androidx.core.view.ViewKt.doOnLayout;
import static androidx.core.widget.TextViewKt.doOnTextChanged;
import static com.jeanbarrossilva.dias.app.Views.push;

public final class DrawerActivity extends Activity {
  SearchSession searchSession;
  DrawerActivityBinding binding;

  @Override
  protected void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    final Launcher launcher = requireLauncher();
    binding = DrawerActivityBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    setUpAppList(launcher);
    setUpSearchField();
  }

  @Override
  @SuppressWarnings("resource")
  protected void onDestroy() {
    super.onDestroy();
    binding = null;
    requireLauncher().endSearchSession(searchSession);
  }

  private Launcher requireLauncher()
    throws ClassCastException, NullPointerException {
    return Dias.requireLauncher(this);
  }

  private void setUpAppList(final Launcher launcher) {
    binding
      .appListView
      .setOnApplyWindowInsetsListener((view, windowInsets) -> {
        doOnLayout(view, laidOutView -> {
          push(laidOutView, windowInsets.getInsets(statusBars()));
          return Unit.INSTANCE;
        });
        return windowInsets;
      });
    binding.appListView.setAdapter(launcher.getHandles());
  }

  @SuppressWarnings({"DataFlowIssue", "resource"})
  private void setUpSearchField() {
    binding
      .searchFieldLayout
      .setOnApplyWindowInsetsListener((layout, windowInsets) -> {
        doOnLayout(layout, laidOutLayout -> {
          push(laidOutLayout, windowInsets.getInsets(navigationBars()));
          return Unit.INSTANCE;
        });
        return windowInsets;
      });
    doOnTextChanged(binding.searchField, (text, start, before, count) -> {
      if (searchSession == null)
        searchSession = requireLauncher().startSearchSession();
      final List<Handle> results = searchSession.search(text.toString());
      binding.appListView.setAdapter(results);
      return Unit.INSTANCE;
    });
    binding.searchField.requestFocus();
    getWindow().getInsetsController().show(ime());
  }
}