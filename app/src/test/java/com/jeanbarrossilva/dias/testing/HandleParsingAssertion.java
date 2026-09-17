package com.jeanbarrossilva.dias.testing;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;

import com.jeanbarrossilva.dias.Handle;
import com.jeanbarrossilva.dias.HandleParser;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractComparableAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ListAssert;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import kotlin.reflect.KProperty1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.list;
import static org.assertj.core.api.InstanceOfAssertFactories.throwable;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.assertj.core.error.ShouldNotHaveThrown.shouldNotHaveThrown;

public final class HandleParsingAssertion
  extends AbstractAssert<HandleParsingAssertion, HandleParser> {
  @NonNull final Context context;
  @NonNull final ActivityInfo activityInfo;

  public static class ThrowsWithUnparsableActivityInfo
    extends AbstractAssert<
              ThrowsWithUnparsableActivityInfo,
              HandleParser.ParsingException.UnparsableActivityInfo
            > {
    private ThrowsWithUnparsableActivityInfo(
      @NonNull final HandleParser.ParsingException.UnparsableActivityInfo actual
    ) {
      super(actual, ThrowsWithUnparsableActivityInfo.class);
    }

    @NonNull
    @SuppressWarnings("rawtypes")
    public ListAssert<KProperty1> properties() {
      return extracting(
        HandleParser.ParsingException.UnparsableActivityInfo::getMissing,
        Assertions.as(list(KProperty1.class))
      );
    }
  }

  private HandleParsingAssertion(
    @NonNull final Context context,
    @NonNull final ActivityInfo activityInfo
  ) {
    super(HandleParser.INSTANCE, HandleParsingAssertion.class);
    this.context = context;
    this.activityInfo = activityInfo;
  }

  @NonNull
  public AbstractComparableAssert<?, Handle> succeeds() {
    try {
      return assertThat(parse());
    } catch (final HandleParser.ParsingException exception) {
      throw assertionError(shouldNotHaveThrown(exception));
    }
  }

  @NonNull
  public ThrowsWithUnparsableActivityInfo throwsWithUnparsableActivityInfo() {
    final HandleParser.ParsingException.UnparsableActivityInfo exception =
      assertThatThrownBy(this::parse)
        .asInstanceOf(
          type(HandleParser.ParsingException.UnparsableActivityInfo.class)
        )
        .actual();
    return new ThrowsWithUnparsableActivityInfo(exception);
  }

  @SuppressLint("CheckResult")
  @SuppressWarnings("ResultOfMethodCallIgnored")
  public void throwsWithNonexistentActivity() {
    assertThatThrownBy(this::parse)
      .asInstanceOf(
        throwable(HandleParser.ParsingException.NonexistentActivity.class)
      );
  }

  @CheckResult
  @NonNull
  public static HandleParsingAssertion assertThatHandleParsing(
    @NonNull final Context context,
    @NonNull final Consumer<@NotNull ActivityInfoBuilder> buildActivityInfo
  ) {
    final var activityInfoBuilder = new ActivityInfoBuilder();
    buildActivityInfo.accept(activityInfoBuilder);
    final ActivityInfo activityInfo = activityInfoBuilder.build();
    return new HandleParsingAssertion(context, activityInfo);
  }

  @NonNull
  private Handle parse() throws HandleParser.ParsingException {
    return HandleParser.parse(context, activityInfo);
  }
}