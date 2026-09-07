package com.jeanbarrossilva.dias;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import java.util.Objects;
import java.util.stream.Stream;

/** Extensions for Android {@link Context}s. */
public final class Contexts {
  private Contexts() {}

  /**
   * Produces a stream containing handles to every user-facing installed
   * application visible to our process.
   *
   * @param self Context for fetching the information on those applications.
   * @throws HandleParser.ParsingException If a handle fails to be instantiated
   *                                       for one of those applications.
   */
  @NonNull
  public static Stream<Handle> queryHandles(@NonNull final Context self)
    throws HandleParser.ParsingException {
    final PackageManager packageManager = self.getPackageManager();
    if (packageManager == null)
      return Stream.empty();
    final Intent intent = new Intent(Intent.ACTION_MAIN, null);
    intent.addCategory(Intent.CATEGORY_LAUNCHER);
    Stream<Handle> result = null;
    try {
      result = packageManager
        .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        .stream()
        .filter(Objects::nonNull)
        .map(resolveInfo -> {
          try {
            return HandleParser.parse(self, resolveInfo.activityInfo);
          } catch (final HandleParser.ParsingException exception) {
            throw new RuntimeException(exception);
          }
        })
        .filter(Objects::nonNull)
        .sorted();
    } catch (final RuntimeException exception) {
      if (exception.getCause() instanceof HandleParser.ParsingException cause)
        throw cause;
    }
    assert result != null;
    return result;
  }
}