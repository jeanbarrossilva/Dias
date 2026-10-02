package com.jeanbarrossilva.dias.core;

import org.assertj.core.api.Assert;
import org.assertj.core.api.InstanceOfAssertFactory;

public class InstanceOfAssertFactories {
  /**
   * Factory that allows converting any {@link Assert} to a {@link
   * OneTimeCopyOnWriteArrayListAssert}.
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  public static final InstanceOfAssertFactory<OneTimeCopyOnWriteArrayList, OneTimeCopyOnWriteArrayListAssert<?>> COPY_ON_WRITE_ARRAY_LIST =
    new InstanceOfAssertFactory<>(
      OneTimeCopyOnWriteArrayList.class,
      OneTimeCopyOnWriteArrayListAssert::assertThat
    );

  private InstanceOfAssertFactories() {}
}