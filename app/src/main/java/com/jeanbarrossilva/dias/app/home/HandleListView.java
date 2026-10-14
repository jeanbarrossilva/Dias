package com.jeanbarrossilva.dias.app.home;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.jeanbarrossilva.dias.Launcher;

import java.util.Optional;

import static java.util.Arrays.asList;

public class HandleListView extends RecyclerView {
  private static final class Adapter
    extends ListAdapter<Launcher.Handle, ViewHolder> {
    private final Launcher launcher;
    private boolean isExpanded;

    private static final DiffUtil.ItemCallback<Launcher.Handle> HANDLE_DIFFING =
      new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areContentsTheSame(
          final Launcher.Handle oldItem,
          final Launcher.Handle newItem
        ) {
          return areItemsTheSame(oldItem, newItem);
        }

        @Override
        public boolean areItemsTheSame(
          final Launcher.Handle oldItem,
          final Launcher.Handle newItem
        ) {
          return oldItem.equals(newItem);
        }
      };

    public Adapter(final Launcher launcher) {
      super(HANDLE_DIFFING);
      this.launcher = launcher;
      if (launcher.hasPins())
        collapse();
      else
        expand();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
      @NonNull final ViewGroup parent,
      final int viewType
    ) throws NullPointerException {
      final Context context = parent.getContext();
      if (context == null)
        throw new NullPointerException("parent.getContext()");
      return new ViewHolder(context);
    }

    @Override
    public void onBindViewHolder(
      @NonNull final ViewHolder holder,
      final int position
    ) {
      final Launcher.Handle handle = getItem(position);
      ((HandleView) holder.itemView).setHandle(handle);
    }

    private void collapse() {
      if (!launcher.hasPins()) {
        isExpanded = false;
        return;
      }
      final Launcher.Handle[] pins = launcher.findPins();
      submitList(asList(pins));
      isExpanded = false;
    }

    private void expand() {
      if (!launcher.hasHandles()) {
        isExpanded = false;
        return;
      }
      submitList(launcher.getHandles());
      isExpanded = true;
    }
  }

  private static final class ViewHolder extends RecyclerView.ViewHolder {
    public ViewHolder(final Context context) {
      super(new HandleView(context));
      final var layoutParams = new LayoutParams(
        /* width = */  LayoutParams.MATCH_PARENT,
        /* height = */ LayoutParams.WRAP_CONTENT
      );
      itemView.setLayoutParams(layoutParams);
    }
  }

  public HandleListView(@NonNull final Context context) {
    this(context, null);
  }

  public HandleListView(
    @NonNull final Context context,
    @Nullable final AttributeSet attrs
  ) {
    this(context, attrs, 0);
  }

  public HandleListView(
    @NonNull final Context context,
    @Nullable final AttributeSet attrs,
    @StyleRes final int defStyleRes
  ) {
    super(context, attrs, defStyleRes);
  }

  public boolean isExpanded() {
    return isExpandedOrNull().orElse(false);
  }

  public void setLauncher(@Nullable final Launcher launcher) {
    if (launcher == null) {
      setAdapter(null);
      setLayoutManager(null);
    } else {
      setAdapter(new Adapter(launcher));
      final Context context = getContext();
      if (context == null || getLayoutManager() instanceof LinearLayoutManager)
        return;
      setLayoutManager(new LinearLayoutManager(context));
    }
  }

  @SuppressWarnings("DataFlowIssue")
  public void toggleExpansion() {
    isExpandedOrNull().ifPresent(isExpanded -> {
      if (isExpanded)
        ((Adapter) getAdapter()).collapse();
      else
        ((Adapter) getAdapter()).expand();
    });
  }

  private Optional<Boolean> isExpandedOrNull() {
    return getAdapter() instanceof Adapter adapter
      ? Optional.of(adapter.isExpanded)
      : Optional.empty();
  }
}