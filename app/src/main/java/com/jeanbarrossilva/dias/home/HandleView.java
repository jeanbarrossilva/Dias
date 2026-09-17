package com.jeanbarrossilva.dias.home;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.widget.AppCompatTextView;

import com.jeanbarrossilva.dias.Handle;
import com.jeanbarrossilva.dias.R;

final class HandleView extends AppCompatTextView {
  private static final int DEFAULT_TEXT_ALIGNMENT = TEXT_ALIGNMENT_VIEW_END;

  public HandleView(@NonNull final Context context) {
    super(context);
    setTextAlignment(DEFAULT_TEXT_ALIGNMENT);
    setTextAppearance(resolveDefaultTextAppearanceResourceID());
    final Resources resources = context.getResources();
    if (resources == null)
      return;
    setTypeface(resources.getFont(R.font.space_grotesk));
  }

  public HandleView(
    @NonNull final Context context,
    @Nullable final AttributeSet attrs
  ) throws UnsupportedOperationException {
    this(context, attrs, 0);
  }

  public HandleView(
    @NonNull final Context context,
    @Nullable final AttributeSet attrs,
    @AttrRes final int defStyleAttr
  ) throws UnsupportedOperationException {
    super(context, attrs, defStyleAttr);
    throw newUnsupportedInflationException();
  }

  public void setHandle(@NonNull final Handle handle) {
    final Context context = getContext();
    if (context == null)
      return;
    setText(handle.label);
    setOnClickListener(clickedView -> {
      final Context launchContext = clickedView.getContext();
      if (launchContext == null)
        return;
      handle.launch(launchContext);
    });
  }

  @StyleRes
  private int resolveDefaultTextAppearanceResourceID() {
    final Context context = getContext();
    if (context == null)
      return Resources.ID_NULL;
    final Resources.Theme theme = context.getTheme();
    if (theme == null)
      return Resources.ID_NULL;
    final var textAppearance = new TypedValue();
    theme.resolveAttribute(
      com.google.android.material.R.attr.textAppearanceBodyMediumEmphasized,
      textAppearance,
      /* resolveRefs = */ true
    );
    return textAppearance.resourceId;
  }

  private UnsupportedOperationException newUnsupportedInflationException() {
    return new UnsupportedOperationException(
      "No support for inflating a HandleView. For now, one can only be " +
        "instantiated programmatically, through the HandleView(Context) " +
        "constructor"
    );
  }
}