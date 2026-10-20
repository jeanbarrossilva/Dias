/*
 * Copyright © 2026 Jean Silva
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *                  https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.jeanbarrossilva.dias.core;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.FutureAssert;
import org.assertj.core.api.InstanceOfAssertFactory;
import org.assertj.core.api.ListAssert;

import java.util.concurrent.Future;

import static com.google.common.util.concurrent.Futures.immediateFuture;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Extensions for AssertJ instance-of-assert factories.
 *
 * @see InstanceOfAssertFactory
 */
public final class InstanceOfAssertFactories {
  private InstanceOfAssertFactories() {}

  /**
   * Factory for converting an array into a list.
   *
   * @param previousClass Class of the array.
   * @param <Element> An element of the array.
   */
  public static <Element> InstanceOfAssertFactory<Element[],
                                                  ListAssert<Element>> listFromArray(
    final Class<Element[]> previousClass
  ) {
    if (previousClass == null)
      return null;
    return new InstanceOfAssertFactory<>(
      previousClass,
      previous -> assertThat(asList(previous))
    );
  }
}