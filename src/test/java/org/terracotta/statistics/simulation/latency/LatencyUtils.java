/*
 * All content copyright Terracotta, Inc., unless otherwise indicated.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terracotta.statistics.simulation.latency;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.terracotta.statistics.Sample;
import org.terracotta.statistics.derived.latency.LatencyHistogramStatistic;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.DoubleUnaryOperator;
import java.util.function.LongSupplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.terracotta.statistics.Sample.sample;
import static org.terracotta.statistics.simulation.latency.StreamUtils.nextGaussian;
import static org.terracotta.statistics.simulation.latency.StreamUtils.takeWhile;
import static org.terracotta.statistics.simulation.latency.StreamUtils.toLong;

/**
 * @author Mathieu Carbou
 */
public class LatencyUtils {

  public static Map<String, BoundedValue> getPercentiles(LatencyHistogramStatistic statistic, DoubleUnaryOperator transform) {
    Map<String, BoundedValue> stats = new LinkedHashMap<>();
    stats.put("min", BoundedValue.min(statistic, transform));
    stats.put("p10", BoundedValue.pct(statistic, transform, .1));
    stats.put("p50", BoundedValue.median(statistic, transform));
    stats.put("p95", BoundedValue.pct(statistic, transform, .95));
    stats.put("p99", BoundedValue.pct(statistic, transform, .99));
    stats.put("max", BoundedValue.max(statistic, transform));
    return stats;
  }

  public static Map<String, BoundedValue> getPercentiles(List<Sample<Long>> samples, DoubleUnaryOperator transform) {
    Map<String, BoundedValue> stats = new LinkedHashMap<>();
    DescriptiveStatistics statistic = new DescriptiveStatistics(samples.stream().mapToDouble(Sample::getSample).toArray());
    stats.put("min", BoundedValue.min(statistic, transform));
    stats.put("p10", BoundedValue.pct(statistic, transform, .1));
    stats.put("p50", BoundedValue.median(statistic, transform));
    stats.put("p95", BoundedValue.pct(statistic, transform, .95));
    stats.put("p99", BoundedValue.pct(statistic, transform, .99));
    stats.put("max", BoundedValue.max(statistic, transform));
    return stats;
  }

  public static List<Sample<Long>> generateLatencies(Random random, double opsPerSec, Duration duration, long meanLatency) {
    final long end = duration.toNanos();
    return takeWhile(latencyStream(random, opsPerSec, meanLatency), sample -> sample.getTimestamp() < end).collect(Collectors.toList());
  }

  public static Stream<Sample<Long>> latencyStream(Random random, double opsPerSec, long latencyMean) {
    // REF: // https://www.javamex.com/tutorials/random_numbers/gaussian_distribution_2.shtml
    // 70% of latencies will fall between latencyMean +/- 20%
    double latencyStdDev = latencyMean / 5.0;
    LongSupplier latencies = toLong(nextGaussian(random, latencyMean, latencyStdDev), Math::round);
    // 70% of delays will fall between delayMean +/- 20%
    double delayMean = 1_000_000_000.0 / opsPerSec; // mean delay in nano sec between operations
    double delayStdDev = delayMean / 5.0;
    LongSupplier delays = toLong(nextGaussian(random, delayMean, delayStdDev), Math::round);
    // build a stream of samples
    return Stream.iterate(sample(0, latencies.getAsLong()), prev -> sample(prev.getTimestamp() + delays.getAsLong(), latencies.getAsLong()));
  }

  public static DoubleUnaryOperator nsToMicros() {
    return value -> value / 1_000.0;
  }

  public static DoubleUnaryOperator nsToMillis() {
    return value -> value / 1_000_000.0;
  }

  public static DoubleUnaryOperator nsToSecs() {
    return value -> value / 1_000_000_000.0;
  }

}
