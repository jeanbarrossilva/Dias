package com.jeanbarrossilva.dias;

import com.google.common.collect.ContiguousSet;

public class Handles {
  public static final Launcher.Handle[] SAMPLES =
    ContiguousSet.closedOpen(0, 13)
                 .stream()
                 .map(
                   index -> new Launcher.Handle(
                     /* packageName = */  "com.jeanbarrossilva.dias",
                     /* activityName = */ "com.jeanbarrossilva.dias.Activity"
                                          + (index + 1),
                     /* label = */        "App " + (index + 1)
                   )
                 )
                 .toArray(Launcher.Handle[]::new);
  public static final Launcher.Handle SAMPLE = SAMPLES[0];

  private Handles() {}
}