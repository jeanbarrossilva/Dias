package com.jeanbarrossilva.dias.internal.benchmarking;

import androidx.benchmark.BlackHole;
import androidx.benchmark.junit4.BenchmarkRule;

import com.jeanbarrossilva.dias.core.OneTimeCopyOnWriteArrayList;

import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;

import static androidx.benchmark.junit4.BenchmarkRuleKt.measureRepeated;
import static com.jeanbarrossilva.dias.core.test.OneTimeCopyOnWriteArrayLists.sampleImmutableTreeSetLikeBackingArray;
import static java.util.Arrays.stream;

@SuppressWarnings("NewClassNamingConvention")
public class OneTimeCopyOnWriteArrayListBenchmark {
  @Rule public BenchmarkRule benchmarkRule = new BenchmarkRule();

  private static final Integer[] BACKING_ARRAY =
    sampleImmutableTreeSetLikeBackingArray(/* length = */ 1_048_576);
  private static final int ELEMENT = BACKING_ARRAY[BACKING_ARRAY.length - 1];

  @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
  @Test
  public void standardArrayListSearch() {
    measureRepeated(benchmarkRule, scope -> {
      final List<Integer> baseList =
        scope.runWithMeasurementDisabled(() -> stream(BACKING_ARRAY).toList());
      final ArrayList<Integer> list =
        scope.runWithMeasurementDisabled(() -> new ArrayList<>(baseList));
      BlackHole.consume(list.indexOf(ELEMENT));
      return Unit.INSTANCE;
    });
  }

  @Test
  public void oneTimeCopyOnWriteArrayListSearch() {
    measureRepeated(benchmarkRule, scope -> {
      final OneTimeCopyOnWriteArrayList<Integer> list =
        scope.runWithMeasurementDisabled(
          () -> new OneTimeCopyOnWriteArrayList<>(
                                           BACKING_ARRAY,
            /* isImmutableTreeSetLike = */ true
          )
        );
      BlackHole.consume(list.indexOf(ELEMENT));
      return Unit.INSTANCE;
    });
  }
}