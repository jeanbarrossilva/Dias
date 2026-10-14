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

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.InstanceOfAssertFactory;
import org.assertj.core.error.AssertionErrorCreator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static java.util.Arrays.stream;
import static org.assertj.core.presentation.StandardRepresentation.STANDARD_REPRESENTATION;

/** Utility class that provides custom Dias assertions. */
public final class Asserts {
  private Asserts() {}

  /**
   * Creates an assertion on the value returned by an instance method of the
   * given object.
   *
   * @param instance Object whose method will be called.
   * @param methodName Name of the method to be called, as declared in the
   *   instance's class or its superclass.
   * @param assertFactory Creator of the returned assertion.
   * @param args Arguments to be passed into the call to the method.
   * @param <Actual> Value returned by the method.
   * @param <Result> Returned assertion.
   * @return An assertion on the method's return value; or {@code null} if
   *   {@code instance}, {@code methodName} or {@code assertFactory} is {@code
   *   null}.
   * @throws AssertionError If the method isn't found, is private or returns a
   *   value that isn't of type {@code Actual}.
   */
  @SuppressWarnings("unchecked")
  public static <Actual, Result extends AbstractAssert<?, Actual>> Result assertThatCall(
    final Object instance,
    final String methodName,
    final InstanceOfAssertFactory<?, Result> assertFactory,
    final Object... args
  ) throws AssertionError {
    if (instance == null || methodName == null || assertFactory == null)
      return null;
    final Class<?> instanceClass = instance.getClass();
    Method method;
    final Class<?>[] parameterTypes = new Class[args.length];
    for (int index = 0; index < args.length; index++) {
      final Object argument = args[index];
      if (argument == null)
        continue;
      parameterTypes[index] = argument.getClass();
    }
    try {
      method = instanceClass.getMethod(methodName, parameterTypes);
    } catch (final NoSuchMethodException exception) {
      try {
        method = instanceClass.getDeclaredMethod(methodName, parameterTypes);
      } catch (final NoSuchMethodException cause) {
        throw new AssertionError(
          formatCall(instance, methodName, parameterTypes) + " not found",
          cause
        );
      }
    }
    final Object uncastReturnValue;
    try {
      uncastReturnValue = method.invoke(instance, args);
    } catch (final IllegalAccessException exception) {
      throw new AssertionError(
        formatCall(instance, methodName, parameterTypes) + " is inaccessible"
      );
    } catch (final InvocationTargetException cause) {
      throw new AssertionError(
        formatCall(instance, methodName, parameterTypes)
          + " threw an exception",
        cause
      );
    }
    String returnTypeName = null;
    Actual castReturnValue = null;
    try {
      if (uncastReturnValue == null
        || Classes.named(returnTypeName = classNameOfInputOf(assertFactory))
                  .isInstance(uncastReturnValue))
        castReturnValue = (Actual) uncastReturnValue;
      else
        throw new AssertionErrorCreator().assertionError(
          /* message = */  formatCall(instance, methodName, parameterTypes)
                             + " returned a value of an unexpected type",
          /* actual = */   uncastReturnValue.getClass().getName(),
          /* expected = */ returnTypeName,
                           STANDARD_REPRESENTATION
        );
    } catch (final ClassNotFoundException unspecificClassCause) {
      ClassNotFoundException finalCause = unspecificClassCause;
      try {
        final Class<?> arrayClass = Classes.named(returnTypeName);
        if (arrayClass != null) {
          finalCause = null;
          if (arrayClass.isInstance(uncastReturnValue))
            castReturnValue = (Actual) uncastReturnValue;
        }
      } catch (final ClassNotFoundException arrayClassCause) {
        finalCause = arrayClassCause;
      }
      if (finalCause != null)
        throw new RuntimeException(finalCause);
    }
    return (Result) assertFactory
      .createAssert(castReturnValue)
      .describedAs(
        instance.toString()
          + '.'
          + methodName
          + '('
          + String.join(", ", stream(args).map(Object::toString).toList())
          + ')'
      );
  }

  private static String formatCall(
    final Object instance,
    final String methodName,
    final Class<?>[] parameterTypes
  ) {
    return instance.toString()
      + '.'
      + methodName
      + '('
      + String.join(
        ", ",
        stream(parameterTypes)
          .map(t -> t == null ? "null" : t.getName())
          .toList()
      )
      + ')';
  }

  private static String classNameOfInputOf(
    final InstanceOfAssertFactory<?, ?> assertFactory
  ) {
    // fragile? `type` and `rawClass` are private in AssertJ v3.27.7; can't
    // access them. this is my workaround, and, evidently, isn't ideal. but
    // works! who'll argue against that?
    return assertFactory.toString().split(" ", /* limit = */ 3)[2];
  }
}