package com.jeanbarrossilva.dias.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
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

import java.util.List;
import java.util.Set;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.jeanbarrossilva.dias.Contexts.queryHandles;
import static java.util.Comparator.comparingInt;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class HandlesView extends RecyclerView {
  private float expansionX = NONE;
  private float expansionY = NONE;
  private int postExpansionTouchedChildViewAdapterPosition = NO_POSITION;

  private static final int NONE = -1;

  private static class Adapter extends RecyclerView.Adapter<ViewHolder> {
    @NonNull private final List<Handle> allHandles;
    @NonNull private final List<Handle> pinnedHandles;
    @NonNull private List<Handle> currentHandles;

    /**
     * Marker that may be set as the tag of a handle view for indicating that
     * its parent has requested an expansion and, therefore, the handle view
     * shouldn't handle the motion event.
     */
    public static class ExpansionMarker {
      @NonNull
      @SuppressWarnings("InstantiationOfUtilityClass")
      public static final ExpansionMarker INSTANCE = new ExpansionMarker();

      private ExpansionMarker() {}
    }

    Adapter(@NonNull final Context context)
      throws HandleParser.ParsingException {
      allHandles = queryHandles(context).toList();
      pinnedHandles = findPinnedHandles(allHandles);
      currentHandles = pinnedHandles;
      setHasStableIds(true);
    }

    @NonNull
    @Override
    @SuppressLint("ClickableViewAccessibility")
    public ViewHolder onCreateViewHolder(
      @NonNull ViewGroup parent,
      int viewType
    ) {
      final Context context = requireNonNull(parent.getContext());
      final var itemView = new HandleView(context);
      itemView.setLayoutParams(
        new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
      );
      itemView.setOnTouchListener(
        (touchedItemView, event) ->
          event.getAction() == MotionEvent.ACTION_UP
            && touchedItemView.getTag() != ExpansionMarker.INSTANCE
            && touchedItemView.performClick()
      );
      return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
      final Handle handle = currentHandles.get(position);
      holder.setHandle(handle);
    }

    @Override
    public int getItemCount() {
      return currentHandles.size();
    }

    @Override
    public long getItemId(int position) {
      return currentHandles.get(position).id;
    }

    public int expand(final int index) {
      assert !isExpanded() : "cannot expand; already expanded";
      assert index >= 0 && index < pinnedHandles.size()
        : index < 0
          ? "cannot expand at a negative index (" + index + ')'
          : "cannot expand at index "
            + index
            + ", as there "
            + (pinnedHandles.size() == 1 ? "is" : "are")
            + " only "
            + pinnedHandles.size()
            + " pinned "
            + (pinnedHandles.size() == 1 ? "handle" : "handles");
      int newIndex = NO_POSITION;
      currentHandles = allHandles;
      for (int absIndex = 0; absIndex < allHandles.size(); absIndex++) {
        final Handle handle = allHandles.get(absIndex);
        if (!handle.isPinned()) {
          notifyItemInserted(absIndex);
          continue;
        }
        final int pinIndex = handle.getPinIndex();
        notifyItemMoved(pinIndex, absIndex);
        if (pinIndex == index)
          newIndex = absIndex;
      }
      assert newIndex > NO_POSITION : "no handle pinned at index " + index;
      return newIndex;
    }

    public void collapse() {
      assert isExpanded() : "cannot collapse; already collapsed";
      currentHandles = pinnedHandles;
      for (int index = 0; index < allHandles.size(); index++) {
        final Handle handle = allHandles.get(index);
        if (handle.isPinned())
          notifyItemMoved(index, handle.getPinIndex());
        else
          notifyItemRemoved(index);
      }
    }

    @NonNull
    private static List<Handle> findPinnedHandles(
      @NonNull final List<Handle> handles
    ) {
      // 1. yes, this seems really inefficient; but
      // 2. we'll likely operate on a small amount of handles: according to my
      //    unscientific, empirical and questionable benchmark, an amount of
      //    handles n, where n ≤ 1,000, will have a negligible impact on
      //    performance with this algorithm.
      //
      //    now, to be a bit more credible: on average, users have 80 apps
      //    installed on their phones (Raas Cloud, 2026). with that in mind,
      //    micro-optimizing this is really unjustifiable.
      return handles.isEmpty()
        ? handles
        : handles
          .stream()
          .filter(Handle::isPinned)
          .sorted(comparingInt(Handle::getPinIndex))
          .collect(toList());
    }

    private void togglePin(final int index) {
      final Handle handle = allHandles.get(index);
      if (handle.isPinned()) {
        final int pinIndex = handle.getPinIndex();
        handle.unpin();
        pinnedHandles.remove(pinIndex);
        for (int j = 0; j < pinnedHandles.size(); j++)
          pinnedHandles.get(j).pin(j);
        if (isExpanded())
          return;
        notifyItemRemoved(pinIndex);
      } else {
        handle.pin(nextPinIndex());
        pinnedHandles.add(handle);
        if (isExpanded())
          return;
        notifyItemInserted(handle.getPinIndex());
      }
    }

    private boolean isExpanded() {
      return currentHandles == allHandles;
    }

    private int nextPinIndex() {
      return pinnedHandles.size();
    }
  }

  public HandlesView(@NonNull final Context context)
    throws HandleParser.ParsingException {
    this(context, null, Resources.ID_NULL);
  }

  public HandlesView(
    @NonNull final Context context,
    @Nullable final AttributeSet attrs
  ) throws HandleParser.ParsingException {
    this(context, attrs, Resources.ID_NULL);
  }

  public HandlesView(
    @NonNull final Context context,
    @Nullable final AttributeSet attrs,
    final int defStyleAttr
  ) throws HandleParser.ParsingException {
    super(context, attrs, defStyleAttr);
    setAdapter(new Adapter(context));
    setLayoutManager(new LinearLayoutManager(context));
    setOverScrollMode(OVER_SCROLL_NEVER);
  }

  private static class ViewHolder extends RecyclerView.ViewHolder {
    ViewHolder(@NonNull TextView itemView) {
      super(itemView);
    }

    void setHandle(@NonNull final Handle handle) {
      if (itemView instanceof HandleView handleView)
        handleView.setHandle(handle);
    }
  }

  @Override
  public boolean onInterceptTouchEvent(MotionEvent e) {
    final int action = e.getActionMasked();
    switch (action) {
      case MotionEvent.ACTION_DOWN -> {
        expansionX = e.getX();
        expansionY = e.getY();
        return true;
      }
      case MotionEvent.ACTION_MOVE -> {
        if (willNotExpand())
          break;
        if (getAdapter() instanceof Adapter adapter) {
          final View child = findChildViewUnder(expansionX, expansionY);
          assert child != null
            : "no child view at (" + expansionX + ", " + expansionY + ')';
          child.setTag(Adapter.ExpansionMarker.INSTANCE);
          final int preExpansionChildViewIndex = getChildAdapterPosition(child);
          expansionX = NONE;
          expansionY = NONE;
          postExpansionTouchedChildViewAdapterPosition =
            expand(adapter, preExpansionChildViewIndex);
          return true;
        }
      }
      case MotionEvent.ACTION_UP -> {
        if (getAdapter() instanceof Adapter adapter && willNotExpand()) {
          if (postExpansionTouchedChildViewAdapterPosition > NO_POSITION) {
            for (int index = 0; index < getChildCount(); index++) {
              final View childView = getChildAt(index);
              if (getChildAdapterPosition(childView) != postExpansionTouchedChildViewAdapterPosition)
                continue;
              childView.setTag(null);
              break;
            }
            postExpansionTouchedChildViewAdapterPosition = NO_POSITION;
          }
          collapse(adapter);
          return true;
        }
      }
    }
    return false;
  }

  public void togglePins(@NonNull final Set<Integer> indices)
    throws IndexOutOfBoundsException {
    if (getAdapter() instanceof Adapter adapter) {
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
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    expansionY = 0;
  }

  private boolean willNotExpand() {
    return expansionX == NONE;
  }

  private int expand(@NonNull final Adapter adapter, final int index) {
    final int postExpansionIndex = adapter.expand(index);
    setOverScrollMode(OVER_SCROLL_ALWAYS);
    smoothScrollToPosition(postExpansionIndex);
    return postExpansionIndex;
  }

  private void collapse(@NonNull final Adapter adapter) {
    adapter.collapse();
    setOverScrollMode(OVER_SCROLL_NEVER);
  }
}