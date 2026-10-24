package com.jeanbarrossilva.dias.launcher.installation;

import com.jeanbarrossilva.dias.launcher.Handle;
import com.jeanbarrossilva.dias.launcher.installation.InstallationDiff.Kind;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

import static com.jeanbarrossilva.dias.core.Arrays.difference;
import static com.jeanbarrossilva.dias.core.Arrays.union;
import static java.util.Arrays.copyOf;
import static org.assertj.core.api.InstanceOfAssertFactories.array;
import static org.springframework.test.util.MethodAssert.assertThat;

@RunWith(Enclosed.class)
public final class InstallationDiffTests {
  private record Configuration(Handle[] sourceHandles, InstallationDiff diff) {
    private interface Tester {
      void accept(final Configuration config)
        throws IllegalAccessException,
               InvocationTargetException,
               NoSuchMethodException;
    }
  }

  @RunWith(RobolectricTestRunner.class)
  public static final class ApplyTests {
    @Test
    public void installation()
      throws IllegalAccessException,
             InvocationTargetException,
             NoSuchMethodException {
      testInstallationDiff(
        Kind.INSTALLATION,
        config -> assertThat(InstallationDiff.class, "merge", Handle[].class)
          .invoke(config.diff(), (Object) config.sourceHandles())
          .asInstanceOf(array(Handle[].class))
          .containsExactly(
            union(
              difference(
                config.sourceHandles(),
                config.diff().handles,
                Handle[].class
              ),
              config.diff().handles,
              Handle[].class
            )
          )
      );
    }

    @Test
    public void replacement()
      throws IllegalAccessException,
             InvocationTargetException,
             NoSuchMethodException {
      testInstallationDiff(Kind.REPLACEMENT, config -> {
        final Handle[] replacedHandles =
          copyOf(config.sourceHandles(), config.sourceHandles().length);
        for (int i = 0; i < config.diff.indices.length; i++) {
          final int j = config.diff.indices[i];
          replacedHandles[j] = config.diff().handles[i];
        }
        assertThat(InstallationDiff.class, "merge", Handle[].class)
          .invoke(config.diff(), (Object) config.sourceHandles())
          .asInstanceOf(array(Handle[].class))
          .containsExactly(replacedHandles);
      });
    }

    @Test
    public void uninstallation()
      throws IllegalAccessException,
             InvocationTargetException,
             NoSuchMethodException {
      testInstallationDiff(Kind.UNINSTALLATION, config -> {
        final Handle[] installedHandles = copyOf(
                         difference(
                           config.sourceHandles(),
                           config.diff().handles,
                           Handle[].class
                         ),
          /* length = */ config.sourceHandles().length
                           - config.diff().handles.length
        );
        assertThat(InstallationDiff.class, "merge", Handle[].class)
          .invoke(config.diff(), (Object) config.sourceHandles())
          .asInstanceOf(array(Handle[].class))
          .containsExactly(installedHandles);
      });
    }
  }

  private static void testInstallationDiff(
    final InstallationDiff.Kind kind,
    final Configuration.Tester tester
  )
    throws IllegalAccessException,
           InvocationTargetException,
           NoSuchMethodException {
    final Handle[] sourceHandles =
      copyOf(Handle.SAMPLES, Handle.SAMPLES.length);
    final var indicesMatrix = new int[][]{
      new int[]{0},
      new int[]{sourceHandles.length / 2},
      new int[]{0, sourceHandles.length / 2, sourceHandles.length - 1}
    };
    for (final int[] indices: indicesMatrix) {
      final Handle[] handles = new Handle[indices.length];
      final String[] handleIDs = new String[indices.length];
      for (int index = 0; index < indices.length; index++) {
        handles[index] = kind == Kind.UNINSTALLATION
          ? sourceHandles[indices[index]]
          : generateHandle();
        handleIDs[index] = handles[index].id;
      }
      final var diff = new InstallationDiff(handles, handleIDs, indices, kind);
      final var config = new Configuration(sourceHandles, diff);
      tester.accept(config);
    }
  }

  private static Handle generateHandle() {
    return new Handle(/* id = */    UUID.randomUUID().toString(),
                      /* label = */ UUID.randomUUID().toString());
  }
}