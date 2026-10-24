package com.jeanbarrossilva.dias.launcher.installation;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;

import androidx.annotation.RestrictTo;

import com.jeanbarrossilva.dias.launcher.Handle;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static java.lang.Math.abs;
import static java.util.Arrays.binarySearch;
import static java.util.Arrays.stream;

public class InstallationChangeReceiver extends BroadcastReceiver {
  private final Handle[] handles;
  private final Consumer<InstallationDiff> listener;

  @RestrictTo(RestrictTo.Scope.LIBRARY)
  public static final IntentFilter FILTER = new IntentFilter();

  static {
    FILTER.addAction(Intent.ACTION_PACKAGE_ADDED);
    FILTER.addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED);
    FILTER.addAction(Intent.ACTION_PACKAGE_REMOVED);
    FILTER.addAction(Intent.ACTION_PACKAGE_REPLACED);
  }

  @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
  public InstallationChangeReceiver(
    final Handle[] handles,
    final Consumer<InstallationDiff> listener
  ) {
    this.handles = handles;
    this.listener = listener;
  }

  @Override
  @SuppressLint("UnsafeProtectedBroadcastReceiver")
  public void onReceive(final Context context, final Intent intent) {
    final PackageManager packageManager = context.getPackageManager();
    if (packageManager == null)
      return;
    final InstallationDiff.Kind diffKind = InstallationDiff.Kind.parse(intent);
    if (diffKind == null)
      return;
    final Uri data = intent.getData();
    if (data == null)
      return;
    final int packageUid =
      intent.getIntExtra(Intent.EXTRA_UID, /* defaultValue = */ -1);
    if (packageUid < 0)
      return;
    final String[] packageNames = packageManager.getPackagesForUid(packageUid);
    if (packageNames == null)
      return;
    final var queriedHandleMap =
      new HashMap<String, Handle>(/* initialCapacity = */ packageNames.length);
    final Intent queryIntent = Handle.createQueryIntent();
    for (final String packageName: packageNames)
      Handle.queryAll(
                            packageManager,
                            queryIntent.setPackage(packageName),
        /* destination = */ queriedHandleMap
      );
    final InstallationDiff diff = new InstallationDiff(
      /* handles = */   queriedHandleMap.values().toArray(Handle[]::new),
      /* handleIDs = */ queriedHandleMap.keySet().toArray(String[]::new),
      /* indices = */   queriedHandleMap
                          .values()
                          .stream()
                          .map(handle -> binarySearch(handles, handle))
                          .mapToInt(
                            index -> index >= 0 ? index : abs(index + 1)
                          )
                          .toArray(),
                        diffKind
    );
    listener.accept(diff);
  }
}