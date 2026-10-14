package com.jeanbarrossilva.dias;

import androidx.annotation.NonNull;

import com.jeanbarrossilva.dias.core.IntHashSets;

import org.agrona.collections.IntHashSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.jeanbarrossilva.dias.core.Asserts.assertThatCall;
import static com.jeanbarrossilva.dias.core.Iterables.getIndices;
import static com.jeanbarrossilva.dias.LauncherAssert.assertThat;
import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

public class LauncherTests {
  @RunWith(RobolectricTestRunner.class)
  public static final class ClosingTests {
    @Test
    public void deregistersOnPinningListener() {
      final var launcher = sampleLauncher();
      final var listenerNotificationCount = new AtomicInteger();
      launcher.setOnPinningListener(
        pinning -> listenerNotificationCount.getAndIncrement()
      );
      launcher.close();
      launcher.pin(0);
      assertThat(listenerNotificationCount)
        .describedAs("listenerNotificationCount")
        .hasValue(0);
    }
  }

  @RunWith(RobolectricTestRunner.class)
  public static final class PinningTests {
    @Test
    public void handlesAreUnpinnedByDefault() {
      final var launcher = sampleLauncher();
      final LauncherAssert launcherAssert = assertThat(launcher);
      for (final int index: getIndices(launcher.getHandles()))
        launcherAssert.hasUnpinned(index);
      launcherAssert.hasHandles().doesNotHavePins().pins().isEmpty();
    }

    @Test
    public void pinsOne() {
      final var launcher = sampleLauncher();
      launcher.pin(0);
      assertThat(launcher)
        .hasHandles()
        .hasPins()
        .hasPinned(0)
        .pins()
        .containsExactly(launcher.getHandles().getFirst());
      launcher.close();
    }

    @Test
    public void pinsOneIdempotently() {
      final var launcher = sampleLauncher();
      launcher.pin(0);
      launcher.pin(0);
      assertThat(launcher)
        .hasHandles()
        .hasPins()
        .hasPinned(0)
        .pins()
        .containsExactly(launcher.getHandles().getFirst());
      launcher.close();
    }

    @Test
    public void pinsVarious() {
      final var launcher = sampleLauncher();
      final IntHashSet pinIndices = IntHashSets.of(0, 1);
      launcher.pin(pinIndices);
      final List<Launcher.Handle> handles = launcher.getHandles();
      assertThat(launcher)
        .hasHandles()
        .hasPins()
        .hasPinned(0)
        .hasPinned(1)
        .pins()
        .containsExactly(handles.getFirst(), handles.get(1));
      launcher.close();
    }

    @Test
    public void pinsVariousIdempotently() {
      final var launcher = sampleLauncher();
      final IntHashSet pinIndices = IntHashSets.of(0, 1);
      launcher.pin(pinIndices);
      launcher.pin(pinIndices);
      final List<Launcher.Handle> handles = launcher.getHandles();
      assertThat(launcher)
        .hasHandles()
        .hasPins()
        .hasPinned(0)
        .hasPinned(1)
        .pins()
        .containsExactly(handles.getFirst(), handles.get(1));
      launcher.close();
    }

    @Test
    public void unpinsOne() {
      final var launcher = sampleLauncher();
      final IntHashSet pinningIndices = IntHashSets.of(0, 1, 2, 3);
      launcher.pin(pinningIndices);
      launcher.unpin(0);
      final List<Launcher.Handle> handles = launcher.getHandles();
      assertThat(launcher)
        .hasHandles()
        .hasPins()
        .hasUnpinned(0)
        .hasPinned(1)
        .hasPinned(2)
        .hasPinned(3)
        .pins()
        .containsExactly(handles.get(1), handles.get(2), handles.get(3));
      launcher.close();
    }

    @Test
    public void unpinsVarious() {
      final var launcher = sampleLauncher();
      final IntHashSet pinningIndices = IntHashSets.of(0, 1, 2, 3);
      launcher.pin(pinningIndices);
      pinningIndices.remove(2);
      pinningIndices.remove(3);
      launcher.unpin(pinningIndices);
      final List<Launcher.Handle> handles = launcher.getHandles();
      assertThat(launcher)
        .hasHandles()
        .hasPins()
        .hasUnpinned(0)
        .hasUnpinned(1)
        .hasPinned(2)
        .hasPinned(3)
        .pins()
        .containsExactly(handles.get(2), handles.get(3));
      launcher.close();
    }

    @Test
    public void listensToPinOfOne() {
      final var launcher = sampleLauncher();
      final var pinningRef = new AtomicReference<Launcher.Pinning>();
      launcher.setOnPinningListener(pinningRef::set);
      launcher.pin(0);
      assertThatCall(pinningRef, "get", as(type(Launcher.Pinning.class)))
        .isEqualTo(new Launcher.Pinning(IntHashSets.of(0), true));
      launcher.close();
    }

    @Test
    public void listensToPinOfSome() {
      final var launcher = sampleLauncher();
      final IntHashSet pinIndices = IntHashSets.of(0, 1);
      final var pinningRef = new AtomicReference<Launcher.Pinning>();
      launcher.setOnPinningListener(pinningRef::set);
      launcher.pin(pinIndices);
      assertThatCall(pinningRef, "get", as(type(Launcher.Pinning.class)))
        .isEqualTo(new Launcher.Pinning(pinIndices, true));
      launcher.close();
    }

    @Test
    public void listensToUnpinOfOne() {
      final var launcher = sampleLauncher();
      final var pinningRef = new AtomicReference<Launcher.Pinning>();
      launcher.pin(0);
      launcher.setOnPinningListener(pinningRef::set);
      launcher.unpin(0);
      assertThatCall(pinningRef, "get", as(type(Launcher.Pinning.class)))
        .isEqualTo(new Launcher.Pinning(IntHashSets.of(0), false));
      launcher.close();
    }

    @Test
    public void listensToUnpinOfSome() {
      final var launcher = sampleLauncher();
      final IntHashSet pinningIndices = IntHashSets.of(0, 1);
      final var pinningRef = new AtomicReference<Launcher.Pinning>();
      launcher.pin(pinningIndices);
      launcher.setOnPinningListener(pinningRef::set);
      launcher.unpin(pinningIndices);
      assertThatCall(pinningRef, "get", as(type(Launcher.Pinning.class)))
        .isEqualTo(new Launcher.Pinning(pinningIndices, false));
      launcher.close();
    }
  }

  @NonNull
  private static Launcher sampleLauncher() {
    return new Launcher(Handles.SAMPLES);
  }
}