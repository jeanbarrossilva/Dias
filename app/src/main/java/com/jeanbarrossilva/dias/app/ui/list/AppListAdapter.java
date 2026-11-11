package com.jeanbarrossilva.dias.app.ui.list;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.jeanbarrossilva.dias.app.ui.HandleView;
import com.jeanbarrossilva.dias.launcher.Handle;

import java.util.List;

final class AppListAdapter
  extends ListAdapter<Handle, AppListAdapter.ViewHolder> {
  private AppListMode mode;

  private static final DiffUtil.ItemCallback<Handle> HANDLE_DIFFING =
    new DiffUtil.ItemCallback<>() {
      @Override
      public boolean areContentsTheSame(
        final Handle oldItem,
        final Handle newItem
      ) {
        return areItemsTheSame(oldItem, newItem);
      }

      @Override
      public boolean areItemsTheSame(
        final Handle oldItem,
        final Handle newItem
      ) {
        return oldItem.equals(newItem);
      }
    };

  static final class ViewHolder extends RecyclerView.ViewHolder {
    private ViewHolder(final Context context) {
      super(new HandleView(context));
      itemView.setLayoutParams(
        new RecyclerView.LayoutParams(
          /* width = */  MATCH_PARENT,
          /* height = */ WRAP_CONTENT
        )
      );
    }
  }

  public AppListAdapter(final AppListMode mode) {
    super(HANDLE_DIFFING);
    setMode(mode);
  }

  @Nullable
  @Override
  public ViewHolder onCreateViewHolder(
    final ViewGroup parent,
    final int viewType
  ) throws NullPointerException {
    final Context context = parent == null ? null : parent.getContext();
    if (context == null)
      return null;
    return new ViewHolder(context);
  }

  @Override
  public void onBindViewHolder(
    final ViewHolder holder,
    final int position
  ) {
    if (holder == null)
      return;
    final Handle handle = getItem(position);
    ((HandleView) holder.itemView).setHandle(handle);
  }

  public AppListMode getMode() {
    return mode;
  }

  public void setMode(final AppListMode mode) {
    if (this.mode != null)
      this.mode.close();
    this.mode = mode;
    final List<Handle> handles = mode.getHandles();
    if (handles != null)
      submitList(handles);
  }
}