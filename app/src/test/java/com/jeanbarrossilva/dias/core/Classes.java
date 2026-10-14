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

import java.lang.reflect.Array;

/** Extensions for {@link Class}es. */
public final class Classes {
  private static final String ARRAY_SUFFIX = "[]";

  private Classes() {}

  /**
   * Returns the class of a primitive, an object or an array type named with the
   * given name.
   *
   * @param name Name of the type whose class will be returned.
   * @throws ClassNotFoundException If no type with the given name is found.
   */
  public static Class<?> named(final String name)
    throws ClassNotFoundException {
    if (name == null)
      return null;
    if (name.endsWith(ARRAY_SUFFIX)) {
      final String elementClassName =
        name.substring(0, name.length() - ARRAY_SUFFIX.length());
      return Array
        .newInstance(named(elementClassName), 0)
        .getClass();
    }
    return switch (name) {
      case "boolean" -> boolean.class;
      case "byte" -> byte.class;
      case "char" -> char.class;
      case "double" -> double.class;
      case "float" -> float.class;
      case "int" -> int.class;
      case "long" -> long.class;
      case "short" -> short.class;
      default -> Class.forName(name);
    };
  }
}