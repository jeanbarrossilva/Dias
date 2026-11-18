package com.jeanbarrossilva.dias.app.util;

import static com.google.common.collect.Sets.difference;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/// Asserter for layout parameters of an Android view.
public final class LayoutParamsAssert extends
  AbstractAssert<LayoutParamsAssert, ViewGroup.LayoutParams> {
  private LayoutParamsAssert(@Nullable final ViewGroup.LayoutParams actual) {
    super(actual, LayoutParamsAssert.class);
  }

  @Override
  public LayoutParamsAssert isEqualTo(@Nullable final Object expected) {
    isNotNull();
    final AbstractAssert<?, ? super ViewGroup.LayoutParams> asserter;
    if (expected instanceof ViewGroup.MarginLayoutParams)
      asserter = super.usingRecursiveComparison(
        RecursiveComparisonConfiguration.builder()
          .withIgnoredFields(
            difference(
              setOfNamesOfFieldsOf(actual()),
              setOfNamesOfFieldsOf(expected)
            )
              .toArray(String[]::new)
          )
          .build()
      );
    else
      asserter = this;
    asserter.isEqualTo(expected);
    return this;
  }

  /// Instantiates a [LayoutParamsAssert].
  ///
  /// @param actual Parameters for which the asserter is.
  /// @return The instantiated asserter.
  @NonNull
  public static LayoutParamsAssert assertThat(
    @Nullable final ViewGroup.LayoutParams actual
  ) {
    return new LayoutParamsAssert(actual).withRepresentation(
      value -> value instanceof final ViewGroup.LayoutParams layoutParams
        ? LayoutParameters.toString(layoutParams)
        : Objects.toString(value)
    );
  }

  private static Set<String> setOfNamesOfFieldsOf(
    @NonNull final Object instance
  ) {
    final Field[] fields = instance.getClass().getFields();
    final var result = new HashSet<String>(fields.length);
    for (final Field field: fields)
      result.add(field.getName());
    return result;
  }
}