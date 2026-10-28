package com.jeanbarrossilva.dias.app.ui;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.AttrRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jeanbarrossilva.OneTimeCopyOnWriteArrayList;
import com.jeanbarrossilva.dias.R;
import com.jeanbarrossilva.dias.launcher.Handle;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class AppListView extends ListView {
  static final class Adapter extends ArrayAdapter<Handle> {
    public Adapter(
      @NonNull final Context context,
      @NonNull final List<Handle> handles
    ) {
      super(context, R.layout.drawer_app_view, handles);
    }

    @NonNull
    @Override
    public View getView(
      final int position,
      @Nullable final View convertView,
      @NonNull final ViewGroup parent
    ) throws NullPointerException {
      final Context context = requireNonNull(getContext(), "getContext()");
      final var result = new HandleView(context);
      final int mSpacing =
        requireNonNull(context.getResources(), "getContext().getResources()")
          .getDimensionPixelSize(R.dimen.spacing_m);
      result.setPadding(
        /* left = */   mSpacing,
        /* top = */    result.getPaddingTop(),
        /* right = */  mSpacing,
        /* bottom = */ result.getPaddingBottom()
      );
      final Handle handle =
        requireNonNull(getItem(position), "getItem(" + position + ')');
      result.setHandle(handle);
      return result;
    }
  }

  public AppListView(@NonNull final Context context) {
    super(context);
  }

  public AppListView(
    @NonNull final Context context,
    @NonNull final AttributeSet attrs
  ) {
    this(context, attrs, 0);
  }

  public AppListView(
    @NonNull final Context context,
    @NonNull final AttributeSet attrs,
    @AttrRes final int defStyleAttr
  ) {
    this(context, attrs, defStyleAttr, Resources.ID_NULL);
  }

  public AppListView(
    @NonNull final Context context,
    @NonNull final AttributeSet attrs,
    @AttrRes final int defStyleAttr,
    @IdRes final int defStyleRes
  ) {
    super(context, attrs, defStyleAttr, defStyleRes);
    if (isInEditMode())
      setAdapter(new OneTimeCopyOnWriteArrayList<>(Handle.createSamples()));
  }

  public void setAdapter(final List<Handle> handles) {
    final Context context = getContext();
    if (context == null)
      return;
    final Adapter adapter = new Adapter(context, handles);
    setAdapter(adapter);
  }
}