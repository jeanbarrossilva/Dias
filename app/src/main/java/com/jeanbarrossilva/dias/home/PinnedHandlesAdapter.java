package com.jeanbarrossilva.dias.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jeanbarrossilva.dias.R;
import com.jeanbarrossilva.dias.databinding.HomePinnedHandleBinding;

import java.util.List;

public class PinnedHandlesAdapter extends ArrayAdapter<Handle> {
  @NonNull final List<Handle> pinnedHandles;

  public PinnedHandlesAdapter(
    @NonNull final Context context,
    @NonNull final List<Handle> objects
  ) {
    super(context, R.layout.home_pinned_handle, objects);
    this.pinnedHandles = objects;
  }

  @NonNull
  @Override
  public View getView(
    int position,
    @Nullable View convertView,
    @NonNull ViewGroup parent
  ) {
    final Handle pinnedHandle = getItem(position);
    if (pinnedHandle == null)
      throw new IndexOutOfBoundsException(String.valueOf(position));
    final TextView updatedView = convertView instanceof TextView
        ? (TextView) convertView
        : HomePinnedHandleBinding
          .inflate(
            LayoutInflater.from(getContext()),
            parent,
            /* attachToParent = */ false
          )
          .getRoot();
    updatedView.setText(pinnedHandle.label);
    return updatedView;
  }
}