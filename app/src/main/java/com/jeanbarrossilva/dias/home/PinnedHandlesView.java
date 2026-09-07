package com.jeanbarrossilva.dias.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jeanbarrossilva.dias.Handle;
import com.jeanbarrossilva.dias.HandleParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.jeanbarrossilva.dias.Contexts.queryHandles;
import static com.jeanbarrossilva.dias.Objects.as;
import static java.util.Arrays.asList;

public class PinnedHandlesView extends RecyclerView {
  private float touchY = 0;

  private static class Adapter extends RecyclerView.Adapter<ViewHolder> {
    @NonNull final List<Handle> allHandles;

    private boolean isExpanded;
    @Nullable private ArrayList<Handle> pinnedHandles;

    Adapter(@NonNull final Context context) throws HandleParser.ParsingException {
      allHandles = queryHandles(context).toList();
      isExpanded = false;
      setHasStableIds(true);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
      @NonNull ViewGroup parent,
      int viewType
    ) {
      final Context context = parent.getContext();
      final Resources.Theme theme = context.getTheme();
      final var itemView = new TextView(context);
      itemView.setLayoutParams(
        new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
      );
      if (theme != null) {
        final var textAppearance = new TypedValue();
        theme.resolveAttribute(
          com.google.android.material.R.attr.textAppearanceBodyMediumEmphasized,
          textAppearance,
          /* resolveRefs = */ true
        );
        itemView.setTextAppearance(textAppearance.resourceId);
      }
      itemView.setTextAlignment(TextView.TEXT_ALIGNMENT_VIEW_END);
      return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
      final Handle handle = getCurrentHandles().get(position);
      holder.setHandle(handle);
    }

    @Override
    public int getItemCount() {
      return getCurrentHandles().size();
    }

    @Override
    public long getItemId(int position) {
      return getCurrentHandles().get(position).id;
    }

    private void togglePin(final int index) {
      if (pinnedHandles == null)
        pinnedHandles = new ArrayList<>();
      final Handle handle = allHandles.get(index);
      if (handle.isPinned()) {
        final int pinIndex = handle.getPinIndex();
        handle.unpin();
        pinnedHandles.remove(pinIndex);
      } else {
        handle.pin(nextPinIndex());
        if (pinnedHandles != null)
          pinnedHandles.add(handle);
      }
    }

    private int nextPinIndex() {
      final ArrayList<Handle> pinnedHandles = this.pinnedHandles;
      if (pinnedHandles == null)
        return 0;
      else
        return pinnedHandles.size();
    }

    @NonNull
    private List<Handle> getCurrentHandles() {
      if (isExpanded)
        return allHandles;
      else {
        if (pinnedHandles == null) {
          // this is really… really… really… well, really bad.
          // I may be low-key very stupid to do this better. note that we want:
          //
          // 1. to allocate an n-sized array in which pinned handles will be
          //    put;
          // 2. to iterate over all handles, find the pinned ones and put them
          //    in that array; and
          // 3. convert that array into an `ArrayList` and set
          //    `this.pinnedHandles` to it.
          //
          // however, there are 2 (two) catches: each handle is pinned *at* a
          // specific index, and we gotta find the index that is the greatest
          // one in order to determine that n ahead of time.
          //
          // as of now, a toddler would've done better than I did in attempting
          // to go through those three simple steps, I fear. (but, hey: it's
          // almost bedtime for me, so I'm really sleepy and can't think
          // straight.)
          final ArrayList<Handle> unorderedPinnedHandles = new ArrayList<>();
          int maxPinIndex = Handle.UNPINNED;
          for (final Handle handle: allHandles) {
            if (!handle.isPinned())
              continue;
            unorderedPinnedHandles.add(handle);
            if (handle.getPinIndex() > maxPinIndex)
              maxPinIndex = handle.getPinIndex();
          }
          final Handle[] pinnedHandles = new Handle[maxPinIndex + 1];
          for (final Handle pinnedHandle: unorderedPinnedHandles)
            pinnedHandles[pinnedHandle.getPinIndex()] = pinnedHandle;
          this.pinnedHandles = (ArrayList<Handle>) asList(pinnedHandles);
        }
        return pinnedHandles;
      }
    }
  }

  public PinnedHandlesView(@NonNull final Context context)
    throws HandleParser.ParsingException {
    this(context, null, Resources.ID_NULL);
  }

  public PinnedHandlesView(
    @NonNull final Context context,
    @Nullable final AttributeSet attrs
  ) throws HandleParser.ParsingException {
    this(context, attrs, Resources.ID_NULL);
  }

  public PinnedHandlesView(
    @NonNull final Context context,
    @Nullable final AttributeSet attrs,
    final int defStyleAttr
  ) throws HandleParser.ParsingException {
    super(context, attrs, defStyleAttr);
    setLayoutManager(new LinearLayoutManager(context));
    setAdapter(new Adapter(context));
  }

  private static class ViewHolder extends RecyclerView.ViewHolder {
    ViewHolder(@NonNull TextView itemView) {
      super(itemView);
    }

    void setHandle(@NonNull final Handle handle) {
      final TextView textView = as(TextView.class, itemView);
      if (textView == null)
        return;
      textView.setTag(handle.id);
      textView.setText(handle.label);
    }
  }

  @Override
  @SuppressLint("ClickableViewAccessibility")
  public boolean onTouchEvent(MotionEvent e) {
    final int action = e.getAction();
    if (action == MotionEvent.ACTION_MOVE || action == MotionEvent.ACTION_DOWN)
      touchY = e.getY();
    return super.onTouchEvent(e);
  }

  public void togglePins(@NonNull final Set<Integer> indices)
    throws IndexOutOfBoundsException {
    final Adapter adapter = as(Adapter.class, getAdapter());
    if (adapter == null)
      return;
    for (final int index: indices) {
      if (index < 0)
        throw new ArrayIndexOutOfBoundsException(index);
      final List<Handle> allHandles = adapter.allHandles;
      if (index >= allHandles.size())
        throw new ArrayIndexOutOfBoundsException(
          "cannot pin handle at index "
            + index
            + ", as there "
            + (allHandles.size() == 1 ? "is" : "are")
            + " only "
            + allHandles.size()
            + " "
            + (allHandles.size() == 1 ? "handle" : "handles")
        );
      adapter.togglePin(index);
    }
  }

  @Override
  protected void onScrollChanged(int l, int t, int oldl, int oldt) {
    final Context context = getContext();
    final Adapter adapter = as(Adapter.class, getAdapter());
    if (context == null || adapter == null) {
      super.onScrollChanged(l, t, oldl, oldt);
      return;
    }
    final ViewHolder expansionViewHolder = findExpansionViewHolder();
    if (expansionViewHolder == null)
      throw new UnsupportedOperationException();
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    touchY = 0;
  }

  @Nullable
  private ViewHolder findExpansionViewHolder() {
    View touchedChildView = findChildViewUnder(0, touchY);
    if (touchedChildView == null)
      return as(ViewHolder.class, findViewHolderForAdapterPosition(0));
    else
      return as(ViewHolder.class, findContainingViewHolder(touchedChildView));
  }
}