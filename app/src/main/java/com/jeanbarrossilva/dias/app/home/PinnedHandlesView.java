package com.jeanbarrossilva.dias.app.home;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jeanbarrossilva.dias.Launcher;

import static java.util.Objects.requireNonNull;

public class PinnedHandlesView extends RecyclerView {
  private static final class Adapter extends RecyclerView.Adapter<ViewHolder> {
    @NonNull private final Launcher launcher;
    @NonNull private final Launcher.Handle[] pins;

    public Adapter(@NonNull final Launcher launcher) {
      this.launcher = launcher;
      this.pins = launcher.findPins();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
      @NonNull final ViewGroup parent,
      final int viewType
    ) {
      final Context context = requireNonNull(parent.getContext());
      final var itemView = new HandleView(context);
      final var layoutParams = new LayoutParams(
        /* width = */  LayoutParams.MATCH_PARENT,
        /* height = */ LayoutParams.WRAP_CONTENT
      );
      itemView.setLayoutParams(layoutParams);
      return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(
      @NonNull final ViewHolder holder,
      final int position
    ) {
      final Launcher.Handle handle = pins[position];
      ((HandleView) holder.itemView).setHandle(launcher, handle);
    }

    @Override
    public int getItemCount() {
      return pins.length;
    }
  }

  private static final class ViewHolder extends RecyclerView.ViewHolder {
    public ViewHolder(@NonNull final HandleView itemView) {
      super(itemView);
    }
  }

  public PinnedHandlesView(@NonNull final Context context) {
    this(context, null);
  }

  public PinnedHandlesView(
    @NonNull final Context context,
    @Nullable final AttributeSet attrs
  ) {
    this(context, attrs, 0);
  }

  public PinnedHandlesView(
    @NonNull final Context context,
    @Nullable final AttributeSet attrs,
    @StyleRes final int defStyleRes
  ) {
    super(context, attrs, defStyleRes);
  }

  public void setLauncher(@Nullable final Launcher launcher) {
    if (launcher == null) {
      setAdapter(null);
      setLayoutManager(null);
    } else {
      setAdapter(new Adapter(launcher));
      if (!(getLayoutManager() instanceof LinearLayoutManager)
        && getContext() instanceof Context context)
        setLayoutManager(new LinearLayoutManager(context));
    }
  }
}