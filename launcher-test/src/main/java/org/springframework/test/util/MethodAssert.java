/*
 * Copyright 2002-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Dias-specific changes:
// • Made the class instantiable from a factory function rather than a public
//   constructor; and
// • added two method-invocation-related assertions: a synchronous one (for
// 	 standard calls) and an asynchronous one (aimed at methods that return a
// 	 future).

package org.springframework.test.util;

import static org.assertj.core.api.InstanceOfAssertFactories.future;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Future;

import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ObjectAssert;

import static java.util.Arrays.stream;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * AssertJ {@link org.assertj.core.api.Assert assertions} that can be applied
 * to a {@link Method}.
 *
 * @author Jean Silva
 * @author Stephane Nicoll
 * @param <Instance> Object on which the method may be invoked.
 * @since 6.2
 */
public class MethodAssert<Instance>
	extends AbstractObjectAssert<MethodAssert<Instance>, Method> {
	private MethodAssert(final Method actual) {
		super(actual, MethodAssert.class);
	}

	/**
	 * Verify that the actual method has the given {@linkplain Method#getName()
	 * name}.
	 * @param name the expected method name
	 */
	public MethodAssert<Instance> hasName(String name) {
		isNotNull();
		Assertions.assertThat(this.actual.getName()).as("Method name").isEqualTo(name);
		return this.myself;
	}

	/**
	 * Verify that the actual method is declared in the given {@code type}.
	 * @param type the expected declaring class
	 */
	public MethodAssert<Instance> hasDeclaringClass(Class<?> type) {
		isNotNull();
		Assertions.assertThat(this.actual.getDeclaringClass())
			.as("Method declaring class").isEqualTo(type);
		return this.myself;
	}

	/**
	 * Returns an asserter on the value resulted from computing the future
   * returned by the method.
	 *
	 * @param instance Object on which the method will be called.
	 * @param args Arguments to be passed into the call to the method.
	 * @throws AssertionError If the method isn't found, is inaccessible or
   *   returns a value that isn't a future.
	 */
	public ObjectAssert<Object> invokeAsync(
		final Instance instance,
		final Object... args
	) throws AssertionError, IllegalAccessException, InvocationTargetException {
    return Assertions.assertThat(getReturnValue(instance, args))
      .describedAs(formatCall(instance))
      .isNotNull()
      .isInstanceOf(Future.class)
      .asInstanceOf(future(Object.class))
      .succeedsWithin(1, MINUTES);
	}

	/**
	 * Returns an assertion on the value returned by an invocation of the method.
	 *
	 * @param instance Object on which the method will be called.
	 * @param args Arguments to be passed into the call to the method.
	 * @throws IllegalAccessException If the method is inaccessible.
	 * @throws InvocationTargetException If the method throws an exception.
	 */
  public ObjectAssert<Object> invoke(
		final Instance instance,
		final Object... args
	) throws IllegalAccessException, InvocationTargetException {
		return Assertions.assertThat(getReturnValue(instance, args))
										 .describedAs(formatCall(instance));
	}

	/**
	 * Instantiates an assertion on the specified method of the given class.
	 *
	 * @param instanceClass Class for whose method the assertion is.
	 * @param name Name of the method as declared in the class or one of the
	 *   class' superclasses.
	 * @param parameterTypes Classes of each argument required by the method, in
	 *   order.
	 * @throws NoSuchMethodException If the method isn't declared in the class or
	 *   one of its superclasses.
	 */
	public static <Instance> MethodAssert<Instance> assertThat(
		final Class<Instance> instanceClass,
		final String name,
		final Class<?>... parameterTypes
	) throws NoSuchMethodException {
		Method actual;
    if (name == null)
      actual = null;
    else {
      try {
        actual = instanceClass.getMethod(name, parameterTypes);
      } catch (final NoSuchMethodException exception) {
        actual = instanceClass.getDeclaredMethod(name, parameterTypes);
      }
    }
		var result = new MethodAssert<Instance>(actual);
    if (instanceClass != null)
      result = result.describedAs(
        formatCall(
          /* instanceRepresentation = */ instanceClass.getSimpleName(),
                                         name,
                                         parameterTypes
        )
      );
    return result;
	}

	private String formatCall(final Instance instance) {
		return instance == null
			? formatCall(
				actual().getDeclaringClass().getSimpleName(),
				actual().getName(),
				actual().getParameterTypes()
			)
			: formatCall(
			  /* instanceRepresentation = */ instance.toString(),
                                       actual().getName(),
                                       actual().getParameterTypes()
			);
	}

	private static String formatCall(
		final String instanceRepresentation,
		final String methodName,
		final Class<?>[] parameterTypes
	) {
		return instanceRepresentation
			+ '.'
			+ methodName
			+ '('
			+ String.join(
        ", ",
        stream(parameterTypes)
          .map(klass -> klass == null ? "null" : klass.getName())
          .toList()
      )
			+ ')';
	}

  private Object getReturnValue(final Instance instance, final Object... args)
    throws InvocationTargetException, IllegalAccessException {
    return isNotNull().actual().invoke(instance, args);
  }
}