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
      setMode(new AppListMode.Pinned(Launcher.sample(context)));
  }

  public void setMode(@Nullable final AppListMode mode) {
    if (mode == null) {
      final AppListMode oldMode = getMode();
      if (oldMode != null)
        oldMode.close();
      setAdapter(null);
      setLayoutManager(null);
      return;
    }
    final Context context = getContext();
    if (context == null)
      return;
    if (getAdapter() instanceof AppListAdapter adapter)
      adapter.setMode(mode);
    else
      setAdapter(new AppListAdapter(mode));
    setLayoutManager(new LinearLayoutManager(context));
  }

  @Nullable
  public AppListMode getMode() {
    return getAdapter() instanceof AppListAdapter adapter
      ? adapter.getMode()
      : null;
  }
}