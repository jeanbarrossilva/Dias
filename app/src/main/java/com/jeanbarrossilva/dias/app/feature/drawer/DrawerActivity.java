package com.jeanbarrossilva.dias.app.feature.drawer;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;

import com.jeanbarrossilva.dias.app.Dias;
import com.jeanbarrossilva.dias.app.ui.list.AppListMode;
import com.jeanbarrossilva.dias.app.ui.list.AppListView;
import com.jeanbarrossilva.dias.app.util.SafeAreaListener;
import com.jeanbarrossilva.dias.databinding.DrawerActivityBinding;
import com.jeanbarrossilva.dias.launcher.Launcher;
import com.jeanbarrossilva.dias.launcher.SearchSession;

import kotlin.Unit;

import static android.view.WindowInsets.Type.ime;
import static androidx.core.widget.TextViewKt.doOnTextChanged;

public final class DrawerActivity extends Activity {
  SearchSession searchSession;
  DrawerActivityBinding binding;

  @Override
  @SuppressWarnings("resource")
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    searchSession = requireLauncher().startSearchSession();
    binding = DrawerActivityBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    setUpAppList();
    setUpSearchField();
  }

  @Override
  @SuppressWarnings("resource")
  protected void onDestroy() {
    super.onDestroy();
    binding = null;
    requireLauncher().endSearchSession(searchSession);
    searchSession = null;
  }

  private Launcher requireLauncher()
    throws ClassCastException, NullPointerException {
    return Dias.requireLauncher(this);
  }

  private void setUpAppList() {
    final AppListView view = binding.appListView;
    view.setOnApplyWindowInsetsListener(SafeAreaListener.statusBars);
    view.setMode(new AppListMode.Search(searchSession));
  }

  @SuppressWarnings("DataFlowIssue")
  private void setUpSearchField() {
    final EditText field = binding.searchField;
    field.setOnApplyWindowInsetsListener(SafeAreaListener.navigationBars);
    doOnTextChanged(field, (text, start, before, count) -> {
      searchSession.search(text.toString());
      return Unit.INSTANCE;
    });
    field.requestFocus();
    getWindow().getInsetsController().show(ime());
  }
}