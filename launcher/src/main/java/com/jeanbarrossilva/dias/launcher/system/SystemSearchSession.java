/*
 * Copyright © 2026 Jean Silva
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see https://www.gnu.org/licenses.
 */

package com.jeanbarrossilva.dias.launcher.system;

import static androidx.appsearch.app.AppSearchEnvironmentFactory.getEnvironmentInstance;
import static androidx.appsearch.app.AppSearchSchema.StringPropertyConfig.INDEXING_TYPE_PREFIXES;

import static com.jeanbarrossilva.dias.launcher.util.ListenableFutures.toCompletableFuture;

import static java.util.Arrays.stream;
import static java.util.Comparator.comparingLong;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.appsearch.annotation.Document;
import androidx.appsearch.app.AppSearchSession;
import androidx.appsearch.app.PutDocumentsRequest;
import androidx.appsearch.app.RemoveByDocumentIdRequest;
import androidx.appsearch.app.SearchResult;
import androidx.appsearch.app.SearchResults;
import androidx.appsearch.app.SearchSpec;
import androidx.appsearch.app.SetSchemaRequest;
import androidx.appsearch.exceptions.AppSearchException;
import androidx.appsearch.localstorage.LocalStorage;

import com.jeanbarrossilva.dias.launcher.Handle;
import com.jeanbarrossilva.dias.launcher.Launcher;
import com.jeanbarrossilva.dias.launcher.SearchSession;
import com.jeanbarrossilva.dias.launcher.installation.InstallationDiff;

import java.io.File;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.IntFunction;

/// Session that delegates search to AndroidX AppSearch.
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public final class SystemSearchSession extends SearchSession {
  private final String indexName;
  private Future<AppSearchSession> delegateFuture;

  /// Instantiator of system sessions from parcels.
  public static Parcelable.Creator<SystemSearchSession> CREATOR =
    new Parcelable.Creator<>() {
      @NonNull
      @Override
      public SystemSearchSession createFromParcel(
        @NonNull final Parcel source
      ) {
        return new SystemSearchSession(source);
      }

      @NonNull
      @Override
      public SystemSearchSession[] newArray(final int size) {
        return new SystemSearchSession[size];
      }
    };

  private static final Runnable NO_OP = () -> {};
  private static final SearchSpec SPEC = new SearchSpec.Builder()
    .addFilterNamespaces(HandleDocument.NAMESPACE)
    .setResultCountPerPage(10000)
    .build();
  private static final SetSchemaRequest SET_SCHEMA_REQUEST;

  static {
    try {
      SET_SCHEMA_REQUEST = new SetSchemaRequest.Builder()
        .addDocumentClasses(HandleDocument.class)
        .build();
    } catch (final AppSearchException cause) {
      throw new RuntimeException(cause);
    }
  }

  @Document
  @SuppressWarnings("ClassCanBeRecord")
  static final class HandleDocument {
    @Document.Namespace
    public final String namespace;

    @Document.Id
    public final String id;

    @Document.StringProperty
    public final String exactLabel;

    @Document.StringProperty(indexingType = INDEXING_TYPE_PREFIXES)
    public final String normalizedLabel;

    @Document.LongProperty
    public final long index;

    private static final String NAMESPACE = "dias";

    public HandleDocument(final Handle handle, final int index) {
      this(NAMESPACE, handle.id, handle.label, normalize(handle.label), index);
    }

    public HandleDocument(
      final String namespace,
      final String id,
      final String exactLabel,
      final String normalizedLabel,
      final long index
    ) {
      this.namespace = namespace;
      this.id = id;
      this.exactLabel = exactLabel;
      this.normalizedLabel = normalizedLabel;
      this.index = index;
    }

    public Handle toHandle() {
      return new Handle(id, exactLabel);
    }
  }

  /// Instantiates a system session from a parceled one.
  ///
  /// @param in The parceled session.
  /// @throws NullPointerException If the parceled session is `null`.
  public SystemSearchSession(@NonNull final Parcel in)
    throws NullPointerException {
    super(in);
    this.indexName = in.readString();
    this.delegateFuture = null;
  }

  /// Instantiates a system session.
  ///
  /// @param context Context in which the index will be created.
  /// @param indexName Name of the database acting as the index backing the
  ///   session. This being the name of another session's index will cause that
  ///   index to be reused in case that session hasn't ended yet.
  /// @see Launcher#endSearchSession(SearchSession)
  public SystemSearchSession(
    @Nullable final Context context,
    @NonNull final String indexName
  ) throws UncontextualizedException {
    super();
    this.indexName = indexName;
    this.delegateFuture = createDelegateFuture(context);
  }

  @Override
  public void writeToParcel(@NonNull final Parcel parcel, final int flags) {
    super.writeToParcel(parcel, flags);
    parcel.writeString(indexName);
  }

  @Override
  protected void onSync(
    @Nullable final Context context,
    @NonNull final Handle[] handles
  ) throws UncontextualizedException {
    final AppSearchSession delegate = getDelegate(context);
    clear(delegate);
    final var futures = new CompletableFuture[handles.length];
    for (int index = 0; index < futures.length; index++)
      futures[index] = putDocumentAsync(delegate, handles[index], index);
    awaitAll(futures);
    await(delegate.requestFlushAsync());
  }

  @Override
  protected void onSync(
    @Nullable final Context context,
    @NonNull final InstallationDiff diff
  ) throws UncontextualizedException {
    final AppSearchSession delegate = getDelegate(context);
    final Handle[] handles = getHandles();
    assert handles != null: "handles != null";
    final IntFunction<CompletableFuture<Void>> synchronizer =
      switch (diff.kind) {
        case INSTALLATION ->
          index -> putDocumentAsync(delegate, handles[index], index);
        case REPLACEMENT ->
          index -> updateDocumentAsync(delegate, handles[index], index);
        case UNINSTALLATION ->
          index -> removeDocumentAsync(delegate, handles[index]);
      };
    awaitAll(
      stream(diff.indices)
        .parallel()
        .mapToObj(synchronizer)
        .toArray(CompletableFuture[]::new)
    );
    await(delegate.requestFlushAsync());
  }

  @NonNull
  @Override
  protected List<Handle> onSearch(
    @Nullable final Context context,
    @NonNull String query
  ) throws UncontextualizedException {
    final SearchResults wrapper = getDelegate(context).search(query, SPEC);
    final List<SearchResult> page = await(wrapper.getNextPageAsync());
    final List<Handle> result = page
      .stream()
      .map(searchResult -> {
        try {
          return searchResult
            .getGenericDocument()
            .toDocumentClass(HandleDocument.class);
        } catch (final AppSearchException cause) {
          throw new RuntimeException(cause);
        }
      })
      .sorted(comparingLong(document -> document.index))
      .map(HandleDocument::toHandle)
      .toList();
    wrapper.close();
    return result;
  }

  @Override
  @SuppressWarnings("ResultOfMethodCallIgnored")
  protected void onEnding(@Nullable final Context context)
    throws UncontextualizedException {
    final AppSearchSession delegate = getDelegate(context);

    @SuppressLint("RestrictedApi")
    final File indexDir = context == null
      ? null
      : getEnvironmentInstance()
      .getAppSearchDir(context, /* userHandle = */ null);

    clear(delegate);
    await(delegate.requestFlushAsync());
    delegate.close();
    if (indexDir != null)
      indexDir.delete();
  }

  @NonNull
  private AppSearchSession getDelegate(@Nullable final Context context)
    throws UncontextualizedException {
    if (delegateFuture == null)
      delegateFuture = createDelegateFuture(context);
    final boolean wasDone = delegateFuture.isDone();
    final AppSearchSession delegate = await(delegateFuture);
    if (!wasDone)
      await(delegate.setSchemaAsync(SET_SCHEMA_REQUEST));
    return delegate;
  }

  @NonNull
  private Future<AppSearchSession> createDelegateFuture(
    @Nullable final Context context
  ) throws UncontextualizedException {
    try {
      assert context != null: "context != null";
      final LocalStorage.SearchContext searchContext =
        new LocalStorage.SearchContext.Builder(context, indexName).build();

      return LocalStorage.createSearchSessionAsync(searchContext);
    } catch (final NullPointerException exception) {
      throw new UncontextualizedException();
    }
  }

  private static <Value> Value await(@NonNull final Future<Value> future) {
    try {
      return future.get();
    } catch (final CancellationException
                 | ExecutionException
                 | InterruptedException cause) {
      throw new RuntimeException(cause);
    }
  }

  private void clear(@NonNull final AppSearchSession delegate) {
    if (hasEnded())
      return;
    final Handle[] handles = getHandles();
    assert handles != null: "handles != null";
    awaitAll(
      stream(handles)
        .map(handle -> removeDocumentAsync(delegate, handle))
        .toArray(CompletableFuture[]::new)
    );
  }

  @SuppressWarnings("rawtypes")
  private static void awaitAll(@NonNull final CompletableFuture[] futures) {
    CompletableFuture.allOf(futures).join();
  }

  @NonNull
  private static CompletableFuture<Void> updateDocumentAsync(
    final AppSearchSession delegate,
    final Handle handle,
    final int index
  ) {
    return removeDocumentAsync(delegate, handle)
      .thenCompose(theVoid -> putDocumentAsync(delegate, handle, index));
  }

  @NonNull
  private static CompletableFuture<Void> putDocumentAsync(
    @NonNull final AppSearchSession delegate,
    @NonNull final Handle handle,
    final int index
  ) {
    try {
      final var document = new HandleDocument(handle, index);
      final PutDocumentsRequest request =
        new PutDocumentsRequest.Builder()
          .addDocuments(document)
          .build();
      return toCompletableFuture(delegate.putAsync(request)).thenRun(NO_OP);
    } catch (final AppSearchException cause) {
      throw new RuntimeException(cause);
    }
  }

  @NonNull
  private static CompletableFuture<Void> removeDocumentAsync(
    @NonNull final AppSearchSession delegate,
    @NonNull final Handle handle
  ) {
    final RemoveByDocumentIdRequest request =
      new RemoveByDocumentIdRequest.Builder(HandleDocument.NAMESPACE)
        .addIds(handle.id)
        .build();
    return toCompletableFuture(delegate.removeAsync(request)).thenRun(NO_OP);
  }
}