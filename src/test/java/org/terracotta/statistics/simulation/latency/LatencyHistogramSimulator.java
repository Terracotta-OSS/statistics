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

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYStepRenderer;
import org.jfree.data.DomainOrder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.terracotta.statistics.Sample;
import org.terracotta.statistics.Time;
import org.terracotta.statistics.derived.latency.DefaultLatencyHistogramStatistic;
import org.terracotta.statistics.simulation.latency.ConcurrentParameterized.ConcurrencyLevel;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Math.round;
import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static java.time.Duration.ofMillis;
import static java.time.Duration.ofMinutes;
import static java.time.Duration.ofSeconds;
import static java.util.function.DoubleUnaryOperator.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.LongStream.iterate;
import static org.junit.runners.Parameterized.Parameters;
import static org.terracotta.statistics.Sample.sample;
import static org.terracotta.statistics.simulation.latency.GraphUtils.writeCSV;
import static org.terracotta.statistics.simulation.latency.GraphUtils.writeGraph;
import static org.terracotta.statistics.simulation.latency.LatencyUtils.generateLatencies;
import static org.terracotta.statistics.simulation.latency.LatencyUtils.getPercentiles;
import static org.terracotta.statistics.simulation.latency.LatencyUtils.nsToMicros;
import static org.terracotta.statistics.simulation.latency.LatencyUtils.nsToSecs;

/**
 * Generates a PNG output from simulated latencies for different kind of histogram settings.
 * <p>
 * Goal is to simulate, for several kind of histogram configurations:
 * <ol>
 * <li>0-9 operations / second over 90 minutes (﻿5400 seconds)</li>
 * <li>Each operation latencies are sent to the histogram implementation</li>
 * <li>A thread is collecting the statistics each 10 seconds (min, max, 50%-ile, 95%-ile, 99%ile)</li>
 * <li>This statistics are displayed in 3 graphs for the last 1-hour, last 30min and last 10min</li>
 * <li>Each graph contains only 20-30 points (so for 10min, 1 point will aggregate 30sec of data and for 1-hour 180sec)</li>
 * </ol>
 *
 * @author Mathieu Carbou
 */
@RunWith(ConcurrentParameterized.class)
//@RunWith(Parameterized.class)
@ConcurrencyLevel(2.0f)
public class LatencyHistogramSimulator {

  private static final Dimension FHD = new Dimension(1920, 1080);
  private static final Dimension HD = new Dimension(1280, 720);

  private static final File OUTPUT = new File("target/latencies");
  private static final ConcurrentMap<File, Map<String, List<Sample<Long>>>> HISTOGRAM_CHART_CREATED = new ConcurrentHashMap<>();
  private static final Duration COLLECTOR_INTERVAL = ofSeconds(10);
  private static final Duration MEAN_LATENCY = ofMillis(100);

  // Sorted list of samples, by time
  private static List<Sample<Long>> OPERATIONS;

  @Parameters(name = "{index}: win={0}_buck={1}_phi={2}_frame={3}_pts={4}")
  public static Iterable<Object[]> data() {

    // SETTINGS FOR: histograms
    double[] PHIS = {0.3, 0.63, 0.7, 0.8, 2.0}; // 0.7 == BarSplittingBiasedHistogram.DEFAULT_PHI
    int[] BUCKETS = {10, 20, 50};
    Duration[] WINDOWS = {ofMinutes(1), ofMinutes(5), ofMinutes(10), ofMinutes(30), ofMinutes(60), ofMinutes(120)};

    // SETTINGS FOR: graph summary in management console
    int[] POINTS = {20, 30, 40}; // number of points to display for each graph
    Duration[] TIME_FRAMES = {ofMinutes(5), ofMinutes(10), ofMinutes(30), ofMinutes(60)}; // time frame of each graph

    // Generates Junit's parameters
    return Stream.of(TIME_FRAMES).flatMap(tf ->
        IntStream.of(POINTS).boxed().flatMap(points ->
            DoubleStream.of(PHIS).boxed().flatMap(phi ->
                IntStream.of(BUCKETS).boxed().flatMap(buckets ->
                    Stream.of(WINDOWS).map(window ->
                        new Object[]{window, buckets, phi, tf, points}
                    )))))
        //.filter(oo -> oo[4].equals(40)) // TO FILTER OUT SOME CASES
        //.limit(40)
        .collect(Collectors.toList());
  }

  @BeforeClass
  public static void prepareFolder() {
    if (OUTPUT.exists()) {
      if (OUTPUT.isFile()) {
        OUTPUT.delete();
      } else {
        Stream.of(OUTPUT.listFiles()).parallel().forEach(File::delete);
      }
    }
    OUTPUT.mkdirs();
    System.out.println("OUTPUT: " + OUTPUT.getAbsolutePath() + lineSeparator());
  }

  @BeforeClass
  public static void simulateOperations() {
    long seed = System.nanoTime();
    System.out.println("SEED: " + seed); // keep this in case test fails so that we can reproduce the failure

    OPERATIONS = generateLatencies(new Random(seed), 15, ofMinutes(90), MEAN_LATENCY.toNanos());
    System.out.println("SIMULATED LATENCIES: " + OPERATIONS.size() + lineSeparator());

    Map<String, BoundedValue> percentiles = getPercentiles(OPERATIONS, nsToMicros());
    System.out.println("LATENCIES PERCENTILES (μs): " + percentiles + lineSeparator());

    // write data, graph and stats on disk
    writeCSV(new File(OUTPUT, "latencies-data.csv"), OPERATIONS);
    writeGraph(
        new File(OUTPUT, "latencies-graph.png"),
        OPERATIONS,
        percentiles,
        DomainOrder.ASCENDING,
        "time (s)", "latency (μs)", FHD,
        nsToSecs(),
        nsToMicros());
  }

  @AfterClass
  public static void openReport() throws IOException {
    // note: only works for mac os users of course!
    new ProcessBuilder("open", "src/test/resources/org/terracotta/statistics/simulation/latency/Latencies.html").start();
  }

  // histogram
  private final double phi;
  private final int bucketCount;
  private final Duration window;
  // graphing
  private final int graphPoints;
  private final Duration graphTimeFrame;

  private long nanoTime = Time.time();

  @Rule public TestName testName = new TestName();

  public LatencyHistogramSimulator(Duration window, int bucketCount, double phi, Duration timeFrame, int points) {
    this.phi = phi;
    this.bucketCount = bucketCount;
    this.window = window;
    this.graphPoints = points;
    this.graphTimeFrame = timeFrame;
  }

  @Test
  public void simulation() {
    // avoid re-creation of the whole histogram and collected sampled
    Map<String, List<Sample<Long>>> samples = HISTOGRAM_CHART_CREATED.computeIfAbsent(getHistogramFile(), histogramFile -> {

      // histogram statistics that will be in Ehcache and will collect all operation's latencies
      DefaultLatencyHistogramStatistic histogram = new DefaultLatencyHistogramStatistic(phi, bucketCount, window, () -> nanoTime);

      // simulate some operations and trigger statistic collection about each 10 sec
      Map<String, List<Sample<Long>>> collectedSamples = replayOperations(histogram);

      //System.out.println("histogram: " + histogram);
      //System.out.println("buckets: " + histogram.buckets()); // prints internal stuff for debugging purposes
      //System.out.println("samples collected: " + collectedSamples.values().stream().mapToInt(List::size).min().orElse(0));

      Map<String, BoundedValue> percentiles = getPercentiles(histogram, nsToMicros());
      writeGraph(
          histogramFile,
          histogram.buckets(),
          percentiles,
          DomainOrder.ASCENDING,
          "latency (μs)", HD,
          nsToMicros());

      return collectedSamples;
    });

    // generate the kind of graph that could be created from the collected samples
    generateGraph(samples);
  }

  private File getHistogramFile() {
    return new File(OUTPUT, format("win=%s_buck=%s_phi=%s_histogram.png", window.toMinutes(), bucketCount, phi));
  }

  private File getGraphFile() {
    return new File(OUTPUT, format("win=%s_buck=%s_phi=%s_frame=%s_pts=%s.png", window.toMinutes(), bucketCount, phi, graphTimeFrame.toMinutes(), graphPoints));
  }

  private Map<String, List<Sample<Long>>> replayOperations(DefaultLatencyHistogramStatistic histogram) {
    final Map<String, List<Sample<Long>>> collectedSamples = new TreeMap<>(); // hold collected samples from histogram each ~10 sec
    final long startNs = nanoTime = Time.time();
    final long duration = COLLECTOR_INTERVAL.toNanos();
    long nextCollect = startNs + duration;
    for (Sample<Long> sample : OPERATIONS) {
      nanoTime = startNs + sample.getTimestamp();
      histogram.event(nanoTime, sample.getSample());
      if (nanoTime >= nextCollect) {
        // collected statistics in voltron each 10 seconds that are sent to a management system
        // collected samples are timestamped by using the "arrival" date on the management system, in milliseconds
        nextCollect = nanoTime + duration;
        getPercentiles(histogram, identity()).forEach((name, pct) -> {
          Sample<Long> collectedSample = sample(sample.getTimestamp(), round(pct.value()));
          collectedSamples.computeIfAbsent(name, s -> new ArrayList<>()).add(collectedSample);
        });
      }
    }
    return collectedSamples;
  }

  private void generateGraph(Map<String, List<Sample<Long>>> collectedSamples) {
    long now = collectedSamples.get("max").get(collectedSamples.get("max").size() - 1).getTimestamp(); // inclusive
    final long windowPerPoint = graphTimeFrame.toNanos() / graphPoints;
    collectedSamples = collectedSamples.entrySet().stream().collect(toMap(
        Map.Entry::getKey,
        e -> reduce(e.getValue(), now, windowPerPoint),
        (u, v) -> {throw new IllegalStateException(String.format("Duplicate key %s", u));},
        LinkedHashMap::new));
    collectedSamples.put("All Latencies", latestSamples(OPERATIONS, now));
    LongSampleDataset dataset = new LongSampleDataset(collectedSamples, DomainOrder.ASCENDING, nsToSecs(), nsToMicros());
    JFreeChart chart = ChartFactory.createXYLineChart("", "time (s)", "latency (μs)", dataset, PlotOrientation.VERTICAL, true, false, false);
    XYPlot plot = (XYPlot) chart.getPlot();
    XYStepRenderer renderer = new XYStepRenderer();
    plot.setRenderer(renderer);
    File graphFile = getGraphFile();
    GraphUtils.writeChart(graphFile, chart, HD);
  }

  private List<Sample<Long>> reduce(List<Sample<Long>> samples, long now, long windowPerPoint) {
    List<Sample<Long>> points = iterate(now, prev -> prev - windowPerPoint)
        .mapToObj(end -> new long[]{end - windowPerPoint, end}) // bounds: ]end-window;end]
        .map(bounds -> sample(bounds[1], samples.stream()
            .filter(sample -> sample.getTimestamp() > bounds[0] && sample.getTimestamp() <= bounds[1])
            .mapToLong(Sample::getSample)
            .max()
            .orElse(0)))
        .limit(graphPoints)
        .collect(Collectors.toList());
    for (int i = 1; i < points.size(); i++) {
      Sample<Long> sample = points.get(i);
      if (sample.getSample() == 0) {
        points.set(i, sample(sample.getTimestamp(), points.get(i - 1).getSample()));
      }
    }
    return points;
  }

  private List<Sample<Long>> latestSamples(List<Sample<Long>> serie, long now) {
    long from = now - graphTimeFrame.toNanos(); // exclusive
    return serie.stream().filter(s -> s.getTimestamp() > from && s.getTimestamp() <= now).collect(Collectors.toList());
  }

}
