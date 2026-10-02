package com.jeanbarrossilva.dias.app.home;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.jeanbarrossilva.dias.Launcher;

import org.apache.commons.lang3.function.BooleanConsumer;

import static java.util.Arrays.asList;

public class HandlesView extends RecyclerView {
  private static final class Adapter
    extends ListAdapter<Launcher.Handle, ViewHolder> {
    private final Launcher launcher;

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
      collapse();
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
      return new ViewHolder(
        context,
        /* expansionToggleRequester = */ (willExpand) -> {
          if (willExpand)
            expand();
          else
            collapse();
        }
      );
    }

    @Override
    public void onBindViewHolder(
      @NonNull final ViewHolder holder,
      final int position
    ) {
      final Launcher.Handle handle = getItem(position);
      ((HandleView) holder.itemView).setHandle(handle);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull final ViewHolder holder) {
      super.onViewDetachedFromWindow(holder);
      holder.resetExpansionState();
    }

    private void collapse() {
      submitList(asList(launcher.findPins()));
    }

    private void expand() {
      submitList(launcher.getHandles());
    }
  }

  private static final class ViewHolder
    extends RecyclerView.ViewHolder
    implements View.OnTouchListener {
    private final BooleanConsumer expansionToggleRequester;
    private ExpansionState expansionState;

    private enum ExpansionState { MAY_EXPAND, EXPANDED }

    public ViewHolder(
      final Context context,
      final BooleanConsumer expansionToggleRequester
    ) {
      super(createItemView(context));
      this.expansionToggleRequester = expansionToggleRequester;
      itemView.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
      if (event != null) {
        switch (event.getActionMasked()) {
          case MotionEvent.ACTION_DOWN ->
            expansionState = ExpansionState.MAY_EXPAND;
          case MotionEvent.ACTION_MOVE -> {
            // notice that our "down" case *always* sets our state to "may
            // expand". however, it's possible for this "move" case to run
            // *without* being preceded by a "down" action (e.g., by a moving
            // mouse cursor); in these scenarios, we simply won't request an
            // expansion.
            if (expansionState != ExpansionState.MAY_EXPAND)
              break;

            this.expansionState = ExpansionState.EXPANDED;
            expansionToggleRequester.accept(true);
          }
          case MotionEvent.ACTION_CANCEL -> {
            requestCollapse();
            return false;
          }
          case MotionEvent.ACTION_UP -> {
            final boolean willClick =
              expansionState == ExpansionState.MAY_EXPAND;
            requestCollapse();
            if (willClick)
              return v.performClick();
          }
        }
      }
      return false;
    }

    public void resetExpansionState() {
      expansionState = null;
    }

    private void requestCollapse() {
      expansionState = null;
      expansionToggleRequester.accept(false);
    }

    private static View createItemView(final Context context) {
      final var itemView = new HandleView(context);
      final var layoutParams = new LayoutParams(
        /* width = */  LayoutParams.MATCH_PARENT,
        /* height = */ LayoutParams.WRAP_CONTENT
      );
      itemView.setLayoutParams(layoutParams);
      return itemView;
    }
  }

  public HandlesView(@NonNull final Context context) {
    this(context, null);
  }

  public HandlesView(
    @NonNull final Context context,
    @Nullable final AttributeSet attrs
  ) {
    this(context, attrs, 0);
  }

  public HandlesView(
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