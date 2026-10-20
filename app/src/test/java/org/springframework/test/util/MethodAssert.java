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

package org.springframework.test.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ObjectAssert;

import static java.util.Arrays.stream;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.concurrent.TimeUnit.MINUTES;

// Dias-specific changes:
// • Allowed instantiation from a factory function rather than a public
//   constructor; and
// • added two method-invocation-related assertions: a synchronous one (for
// 	 standard calls) and an asynchronous one (aimed at methods that return a
// 	 future).

/**
 * AssertJ {@link org.assertj.core.api.Assert assertions} that can be applied
 * to a {@link Method}.
 *
 * @author Jean Silva
 * @author Stephane Nicoll
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
	 * Creates an assertion on the value of the future returned by the method.
	 *
	 * @param instance Object on which method will be called.
	 * @param args Arguments to be passed into the call to the method.
	 * @param <Actual> Object held by the future returned by the method.
	 * @return An assertion on the method's returned future; or {@code null} if
	 *   {@code assertFactory} is {@code null}.
	 * @throws AssertionError If the method isn't found, is private or returns a
	 *   value that isn't a future that holds an {@link Actual}.
	 */
	@SuppressWarnings("unchecked")
	public <Actual,
		Result extends AbstractAssert<?, ?>> Result invokeAsync(
		final Instance instance,
		final Object... args
	) throws AssertionError {
		final ExecutorService executor = newCachedThreadPool();
		final Result result;
		try {
			result = (Result) Assertions
				.assertThat(executor.submit(() -> invoke(instance, args)))
				.describedAs("future of " + formatCall(instance))
				.succeedsWithin(1, MINUTES);
		} catch (final RejectedExecutionException cause) {
			throw new AssertionError(
				/* message = */ "submission of future returned by "
													+ formatCall(instance)
													+ " threw an exception",
												cause
			);
		} finally {
			executor.close();
		}
		return result;
	}

	/**
	 * Creates an assertion on the value returned by an invocation of the method.
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
		final Object returnValue = actual().invoke(instance, args);
		return Assertions.assertThat(returnValue)
										 .describedAs(formatCall(instance));
	}

	/**
	 * Instantiates an assertion on the specified method of the given class.
	 *
	 * @param klass Class for whose method the assertion is.
	 * @param name Name of the method as declared in the class or one of the
	 *   class' superclasses.
	 * @param parameterTypes Classes of each argument required by the method, in
	 *   order.
	 * @throws NoSuchMethodException If the method isn't declared in the class or
	 *   one of its superclasses.
	 */
	public static <Instance> MethodAssert<Instance> assertThat(
		final Class<Instance> klass,
		final String name,
		final Class<?>... parameterTypes
	) throws NoSuchMethodException {
		if (klass == null || name == null)
			return null;
		final Method actual = klass.getMethod(name, parameterTypes);
		return new MethodAssert<Instance>(actual)
			.as(formatCall(/* prefix = */ klass.toString(), name, parameterTypes));
	}

	private String formatCall(final Object instance) {
		return formatCall(
			/* prefix = */ instance.toString(),
										 actual().getName(),
										 actual().getParameterTypes()
		);
	}

	private static String formatCall(
		final String prefix,
		final String methodName,
		final Class<?>[] parameterTypes
	) {
		return prefix
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
}