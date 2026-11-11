package com.jeanbarrossilva.dias.app.ui.list;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jeanbarrossilva.dias.launcher.Launcher;
import com.jeanbarrossilva.dias.launcher.SearchSession;

public final class AppListView extends RecyclerView {
  public AppListView(@NonNull final Context context) {
    super(context);
  }

  public AppListView(
    @NonNull final Context context,
    @Nullable final AttributeSet attrs
  ) {
    this(context, attrs, /* defStyleRes = */ Resources.ID_NULL);
  }

  public AppListView(
    @NonNull final Context context,
    @Nullable final AttributeSet attrs,
    @StyleRes final int defStyleRes
  ) {
    super(context, attrs, defStyleRes);
    if (isInEditMode())
      switchToPinnedMode(Launcher.sample(context));
  }

  public void switchToPinnedMode(@Nullable final Launcher launcher) {
    setMode(new AppListMode.Pinned(launcher));
  }

  public void switchToSearchMode(@Nullable final SearchSession searchSession) {
    setMode(new AppListMode.Search(searchSession, results -> {
      if (getAdapter() instanceof AppListAdapter adapter)
        adapter.submitList(results);
    }));
  }

  private void setMode(final AppListMode mode) {
    final Context context = getContext();
    if (context == null)
      return;
    if (getAdapter() instanceof AppListAdapter adapter)
      adapter.setMode(mode);
    else
      setAdapter(new AppListAdapter(mode));
    setLayoutManager(new LinearLayoutManager(context));
  }
}