package com.jeanbarrossilva.dias.launcher;

import com.jeanbarrossilva.dias.core.IntHashSets;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.springframework.test.util.MethodAssert;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.jeanbarrossilva.dias.launcher.LauncherAssert.assertThat;
import static java.util.Arrays.stream;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.assertj.core.api.Assertions.assertThat;

public class LauncherTests {
  @RunWith(RobolectricTestRunner.class)
  public static final class ClosingTests {
    @Test
    public void deregistersOnPinningListener() {
      final ExecutorService executor = createExecutor();
      final Launcher launcher = Launcher.sample(getApplicationContext());
      final Handle pin = launcher.getHandles().getFirst();
      final var listenerNotificationCount = new AtomicInteger();
      launcher.setOnPinningListener(
        pinning -> listenerNotificationCount.getAndIncrement());
      launcher.close();
      executor.submit(() -> launcher.pin(pin.id));
      executor.close();
      assertThat(listenerNotificationCount)
        .describedAs("listenerNotificationCount")
        .hasValue(0);
    }
  }

  @RunWith(RobolectricTestRunner.class)
  public static final class PinningTests {
    @Test
    public void handlesAreUnpinnedByDefault() {
      final Launcher launcher = Launcher.sample(getApplicationContext());
      final LauncherAssert launcherAssert = assertThat(launcher);
      for (final Handle handle: launcher.getHandles())
        launcherAssert.hasUnpinned(handle.id);
      launcherAssert.hasHandles()
                    .doesNotHavePins()
                    .pins()
                    .isEmpty();
    }

    @Test
    public void pinsOne() {
      final ExecutorService executor = createExecutor();
      final Launcher launcher = Launcher.sample(getApplicationContext());
      final Handle pin = launcher.getHandles().getFirst();
      executor.submit(() -> launcher.pin(pin.id));
      executor.close();
      assertThat(launcher).hasHandles()
                          .hasPins()
                          .hasPinned(pin.id)
                          .pins()
                          .containsExactly(launcher.getHandles().getFirst());
      launcher.clear();
      launcher.close();
    }

    @Test
    public void pinsOneIdempotently() {
      final ExecutorService executor = createExecutor();
      final Launcher launcher = Launcher.sample(getApplicationContext());
      final List<Handle> handles = launcher.getHandles();
      final Handle pin = handles.getFirst();
      executor.submit(() -> {
        launcher.pin(pin.id);
        launcher.pin(pin.id);
      });
      executor.close();
      assertThat(launcher).hasHandles()
                          .hasPins()
                          .hasPinned(pin.id)
                          .pins()
                          .containsExactly(handles.getFirst());
      launcher.clear();
      launcher.close();
    }

    @Test
    public void pinsVarious() {
      final ExecutorService executor = createExecutor();
      final Launcher launcher = Launcher.sample(getApplicationContext());
      final List<Handle> handles = launcher.getHandles();
      final List<String> pinIDs = handles.stream()
                                         .map(handle -> handle.id)
                                         .limit(2)
                                         .toList();
      executor.submit(() -> launcher.pin(new HashSet<>(pinIDs)));
      executor.close();
      assertThat(launcher).hasHandles()
                          .hasPins()
                          .hasPinned(pinIDs.getFirst())
                          .hasPinned(pinIDs.getLast())
                          .pins()
                          .containsExactly(handles.getFirst(), handles.get(1));
      launcher.clear();
      launcher.close();
    }

    @Test
    public void pinsVariousIdempotently() {
      final ExecutorService executor = createExecutor();
      final Launcher launcher = Launcher.sample(getApplicationContext());
      final List<Handle> handles = launcher.getHandles();
      final List<String> handleIDs = handles.stream()
                                            .map(handle -> handle.id)
                                            .limit(2)
                                            .toList();
      executor.submit(() -> handleIDs.forEach(launcher::pin));
      executor.close();
      assertThat(launcher).hasHandles()
                          .hasPins()
                          .hasPinned(handleIDs.getFirst())
                          .hasPinned(handleIDs.getLast())
                          .pins()
                          .containsExactly(handles.getFirst(), handles.get(1));
      launcher.clear();
      launcher.close();
    }

    @Test
    public void unpinsOne() {
      final ExecutorService executor = createExecutor();
      final Launcher launcher = Launcher.sample(getApplicationContext());
      final List<String> pinningIDs = launcher.getHandles()
                                              .stream()
                                              .map(handle -> handle.id)
                                              .limit(4)
                                              .toList();
      final HashSet<String> pinningIDsAsSet = new HashSet<>(pinningIDs);
      executor.submit(() -> {
        launcher.pin(pinningIDsAsSet);
        launcher.unpin(pinningIDs.getFirst());
      });
      executor.close();
      final List<Handle> handles = launcher.getHandles();
      assertThat(launcher).hasHandles()
                          .hasPins()
                          .hasUnpinned(pinningIDs.getFirst())
                          .hasPinned(pinningIDs.get(1))
                          .hasPinned(pinningIDs.get(2))
                          .hasPinned(pinningIDs.get(3))
                          .pins()
                          .containsExactly(handles.get(1),
                                           handles.get(2),
                                           handles.get(3));
      launcher.clear();
      launcher.close();
    }

    @Test
    public void unpinsVarious() {
      final ExecutorService executor = createExecutor();
      final Launcher launcher = Launcher.sample(getApplicationContext());
      final List<String> pinningHandleIDs = launcher.getHandles()
                                                    .stream()
                                                    .map(handle -> handle.id)
                                                    .limit(4)
                                                    .collect(toList());
      executor.submit(() -> launcher.pin(new HashSet<>(pinningHandleIDs)));
      executor.close();
      final String[] immortalPinIDs = new String[]{
        pinningHandleIDs.remove(1),
        pinningHandleIDs.remove(2)
      };
      launcher.unpin(new HashSet<>(pinningHandleIDs));
      assertThat(launcher).hasHandles()
                          .hasPins()
                          .hasUnpinned(pinningHandleIDs.getFirst())
                          .hasUnpinned(pinningHandleIDs.get(1))
                          .hasPinned(immortalPinIDs[0])
                          .hasPinned(immortalPinIDs[1])
                          .pins()
                          .containsExactly(
                            stream(immortalPinIDs).map(launcher::get)
                                                  .toArray(Handle[]::new));
      launcher.clear();
      launcher.close();
    }

    @Test
    public void listensToPinOfOne()
      throws IllegalAccessException,
             InvocationTargetException,
             NoSuchMethodException {
      final ExecutorService executor = createExecutor();
      final Launcher launcher = Launcher.sample(getApplicationContext());
      final Handle pin = launcher.getHandles().getFirst();
      final var pinningRef = new AtomicReference<Launcher.Pinning>();
      launcher.setOnPinningListener(pinningRef::set);
      executor.submit(() -> launcher.pin(pin.id));
      executor.close();
      MethodAssert.assertThat(AtomicReference.class, "get")
                  .invoke(pinningRef)
                  .isEqualTo(new Launcher.Pinning(IntHashSets.of(0), true));
      launcher.clear();
      launcher.close();
    }

    @Test
    public void listensToPinOfSome()
      throws IllegalAccessException,
             InvocationTargetException,
             NoSuchMethodException {
      final ExecutorService executor = createExecutor();
      final Launcher launcher = Launcher.sample(getApplicationContext());
      final Set<String> pinIDs = launcher.getHandles()
                                         .stream()
                                         .map(handle -> handle.id)
                                         .limit(2)
                                         .collect(toUnmodifiableSet());
      final var pinningRef = new AtomicReference<Launcher.Pinning>();
      launcher.setOnPinningListener(pinningRef::set);
      executor.submit(() -> launcher.pin(pinIDs));
      executor.close();
      MethodAssert.assertThat(AtomicReference.class, "get")
                  .invoke(pinningRef)
                  .isEqualTo(new Launcher.Pinning(IntHashSets.of(0, 1), true));
      launcher.clear();
      launcher.close();
    }

    @Test
    public void listensToUnpinOfOne()
      throws IllegalAccessException,
             InvocationTargetException,
             NoSuchMethodException {
      final ExecutorService executor = createExecutor();
      final Launcher launcher = Launcher.sample(getApplicationContext());
      final Handle pin = launcher.getHandles().getFirst();
      final var pinningRef = new AtomicReference<Launcher.Pinning>();
      executor.submit(() -> launcher.pin(pin.id));
      launcher.setOnPinningListener(pinningRef::set);
      executor.submit(() -> launcher.unpin(pin.id));
      executor.close();
      MethodAssert.assertThat(AtomicReference.class, "get")
                  .invoke(pinningRef)
                  .isEqualTo(new Launcher.Pinning(IntHashSets.of(0), false));
      launcher.clear();
      launcher.close();
    }

    @Test
    public void listensToUnpinOfSome()
      throws IllegalAccessException,
             InvocationTargetException,
             NoSuchMethodException {
      final ExecutorService executor = createExecutor();
      final Launcher launcher = Launcher.sample(getApplicationContext());
      final Set<String> pinningIDs = launcher.getHandles()
                                             .stream()
                                             .map(handle -> handle.id)
                                             .limit(2)
                                             .collect(toUnmodifiableSet());
      final var pinningRef = new AtomicReference<Launcher.Pinning>();
      executor.submit(() -> launcher.pin(pinningIDs));
      launcher.setOnPinningListener(pinningRef::set);
      executor.submit(() -> launcher.unpin(pinningIDs));
      executor.close();
      MethodAssert.assertThat(AtomicReference.class, "get")
                  .invoke(pinningRef)
                  .isEqualTo(new Launcher.Pinning(IntHashSets.of(0, 1), false));
      launcher.clear();
      launcher.close();
    }
  }

  private static ExecutorService createExecutor() {
    return newCachedThreadPool();
  }
}