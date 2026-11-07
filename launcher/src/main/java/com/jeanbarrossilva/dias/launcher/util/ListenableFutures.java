package com.jeanbarrossilva.dias.launcher.util;

import static com.google.common.util.concurrent.Futures.addCallback;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.CompletableFuture;

/** Extensions for {@link ListenableFuture}s. */
public final class ListenableFutures {
  private ListenableFutures() {}

  /**
   * Converts a listenable future into a completable one.
   *
   * @param self Future to be made completable.
   * @return The completable future into which the listenable, given one was
   *   converted, or {@code null} if {@code self} was {@code null}.
   * @param <Value> Object that may be computed by the future.
   */
  public static <Value> CompletableFuture<Value> toCompletableFuture(
    final ListenableFuture<Value> self
  ) {
    if (self == null)
      return null;
    final var result = new CompletableFuture<Value>();
    addCallback(
      self,
      new FutureCallback<Value>() {
        @Override
        public void onSuccess(final Value value) {
          result.complete(value);
        }

        @Override
        public void onFailure(Throwable failure) {
          result.completeExceptionally(failure);
        }
      },
      directExecutor()
    );
    return result;
  }
}