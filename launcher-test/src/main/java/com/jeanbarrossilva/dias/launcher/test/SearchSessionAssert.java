/*
 * Copyright © 2026 Jean Silva
 *
 * Dias is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 2 of the License, or (at your option) any later
 * version.
 *
 * Dias is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Dias. If not, see https://www.gnu.org/licenses.
 */

package com.jeanbarrossilva.dias.launcher.test;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jeanbarrossilva.dias.launcher.Handle;
import com.jeanbarrossilva.dias.launcher.SearchSession;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ListAssert;

import java.util.List;
import java.util.function.Consumer;

/// Asserter for a [SearchSession].
public final class SearchSessionAssert
  extends AbstractAssert<SearchSessionAssert, SearchSession> {
  private final Consumer<List<Handle>> onSearchListener;
  private List<Handle> results;

  private SearchSessionAssert(final SearchSession actual) {
    super(actual, SearchSessionAssert.class);
    this.onSearchListener = results -> this.results = results;
    this.results = null;
  }

  /// Searches for handles that match the given query.
  ///
  /// @param query Text to search handles by.
  /// @throws AssertionError If the session is `null`.
  /// @throws SearchSession.EndedException If the session has ended.
  @NonNull
  public SearchSessionAssert search(@Nullable final String query)
    throws AssertionError, SearchSession.EndedException {
    isNotNull().actual().search(getApplicationContext(), query);
    return this;
  }

  /// Asserts that the results of searches have never been notified to this
  /// asserter.
  ///
  /// @see #listenToSearches()
  @NonNull
  public SearchSessionAssert doesNotHaveResults() {
    results().isNull();
    return this;
  }

  /// Returns an asserter for the results of the last search in the session. The
  /// list of the returned asserter will be `null` in case searches were never
  /// listened to by and notified to this asserter.
  ///
  /// @see #listenToSearches()
  @NonNull
  public ListAssert<Handle> results() throws AssertionError {
    return Assertions.assertThat(results)
      .describedAs(
        (getWritableAssertionInfo().hasDescription()
          ? descriptionText()
          : isNotNull().actual().toString())
          + ".results"
      );
  }

  /// Sets the on-search listener of the session to one that stores the results
  /// in this asserter, updating them at every search; these results may be
  /// asserted on through the [#results()] method.
  ///
  /// If this present method never gets called, or does but no searches are
  /// performed afterward, [#results()] will hold a `null` list.
  ///
  /// @throws AssertionError If the session is `null`.
  @NonNull
  public SearchSessionAssert listenToSearches() throws AssertionError {
    isNotNull().actual().setOnSearchListener(onSearchListener);
    return this;
  }

  /// Instantiates an asserter for the given search session.
  ///
  /// @param actual Search session for which the asserter is.
  /// @return The instantiated asserter.
  @NonNull
  public static SearchSessionAssert assertThat(
    @Nullable final SearchSession actual
  ) {
    return new SearchSessionAssert(actual);
  }
}