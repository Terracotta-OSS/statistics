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
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.DomainOrder;
import org.terracotta.statistics.derived.histogram.Histogram;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.Collectors;

/**
 * @author Mathieu Carbou
 */
public class GraphUtils {

  private static final NumberFormat FORMATER = NumberFormat.getNumberInstance();

  static {
    FORMATER.setMaximumFractionDigits(0);
  }

  public static void writeCSV(File output, List<LongSample> samples) {
    try (Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output, false), StandardCharsets.UTF_8), 64 * 1024)) {
      out.write("time delta (ns),latency (ns)\n");
      for (LongSample operation : samples) {
        out.write(operation.time() + "," + operation.value() + "\n");
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static void writePercentiles(File output, Map<String, Double> percentiles) {
    try (Writer out = new OutputStreamWriter(new FileOutputStream(output, false), StandardCharsets.UTF_8)) {
      for (Map.Entry<String, Double> entry : percentiles.entrySet()) {
        out.write(entry.getKey() + "=" + entry.getValue() + "\n");
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static void writeGraph(File output,
                                List<LongSample> samples, Map<String, BoundedValue> percentiles, DomainOrder domainOrder,
                                String xAxisLabel, String yAxisLabel, Dimension dimension,
                                DoubleUnaryOperator xAxisTransform, DoubleUnaryOperator yAxisTransform) {
    String title = String.format("count: %s, min: %s, max: %s%n%s",
        samples.size(),
        percentiles.get("min"),
        percentiles.get("max"),
        stringify(percentiles));
    LongSampleDataset dataset = new LongSampleDataset(samples, domainOrder, xAxisTransform, yAxisTransform);
    JFreeChart chart = ChartFactory.createXYLineChart(title, xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL, true, false, false);
    XYPlot plot = (XYPlot) chart.getPlot();
    plot.setFixedLegendItems(new LegendItemCollection());
    addMarkers(percentiles, plot);
    writeChart(output, chart, dimension);
  }

  public static void writeGraph(File output,
                                List<Histogram.Bucket> buckets, Map<String, BoundedValue> percentiles, DomainOrder domainOrder,
                                String xAxisLabel, Dimension dimension,
                                DoubleUnaryOperator xAxisTransform) {
    String title = String.format("count: %s, min: %s, max: %s%n%s",
        (long) buckets.stream().mapToDouble(Histogram.Bucket::count).sum(),
        percentiles.get("min"),
        percentiles.get("max"),
        stringify(percentiles));
    HistogramDataset dataset = new HistogramDataset("", buckets, domainOrder, xAxisTransform);
    JFreeChart chart = ChartFactory.createXYBarChart(title, xAxisLabel, false, "height", dataset, PlotOrientation.VERTICAL, true, false, false);
    XYPlot plot = (XYPlot) chart.getPlot();
    plot.setFixedLegendItems(new LegendItemCollection());
    XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
    renderer.setDrawBarOutline(true);
    writeChart(output, chart, dimension);
  }

  private static void addMarkers(Map<String, BoundedValue> percentiles, XYPlot plot) {
    for (Map.Entry<String, BoundedValue> entry : percentiles.entrySet()) {
      ValueMarker marker = new ValueMarker(entry.getValue().value());
      marker.setPaint(Color.black);
      marker.setLabel(entry.getKey() + ": " + entry.getValue().toString(FORMATER));
      marker.setLabelFont(new Font("SansSerif", Font.BOLD, 12));
      marker.setLabelOffset(new RectangleInsets(3.0, 5, 5.0, 3.0));
      marker.setLabelTextAnchor(TextAnchor.BOTTOM_LEFT);
      plot.addRangeMarker(marker);
    }
  }

  public static void writeChart(File output, JFreeChart chart, Dimension dimension) {
    try {
      ChartUtils.saveChartAsPNG(output, chart, dimension.width, dimension.height);
    } catch (RuntimeException e) {
      // this is because of a JFreeChart issue: if it fails with a runtime exception, it leaves an empty file on the disk
      output.delete();
      throw e;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static String stringify(Map<String, BoundedValue> percentiles) {
    return percentiles.entrySet()
        .stream()
        .filter(e -> !e.getKey().equals("min") && !e.getKey().equals("max"))
        .map(e -> e.getKey() + ": " + e.getValue().toString(FORMATER))
        .collect(Collectors.joining("\n"));
  }

}
