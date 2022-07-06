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

package org.terracotta.statistics.derived.histogram;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import static java.lang.Math.min;
import static java.util.Arrays.asList;
import static java.util.Comparator.comparingDouble;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.of;
import static org.terracotta.statistics.derived.histogram.BarSplittingBiasedHistogram.nextUpIfEqual;

public class StripedHistogram extends Striped<BarSplittingBiasedHistogram> implements Histogram {

  public StripedHistogram(double maxCoefficient, double phi, int expansionFactor, int bucketCount, double barEpsilon, long window) {
    super(() -> new BarSplittingBiasedHistogram(maxCoefficient, phi, expansionFactor, bucketCount, barEpsilon, window));
  }

  public StripedHistogram(int bucketCount, long window) {
    super(() -> new BarSplittingBiasedHistogram(bucketCount, window));
  }

  public StripedHistogram(double phi, int bucketCount, long window) {
    super(() -> new BarSplittingBiasedHistogram(phi, bucketCount, window));

  }

  @Override
  public List<Bucket> getBuckets() {
    List<double[]> bars = stream().flatMap(h -> h.bars().stream().map(bar -> new double[] {bar.minimum(), bar.maximum(), bar.count()}))
        .sorted(Comparator.<double[]>comparingDouble(triple -> triple[0])).collect(toCollection(LinkedList::new));

    mergeBars(bars);

    int bucketCount = stream().findAny().map(BarSplittingBiasedHistogram::bucketCount).orElseThrow(AssertionError::new);
    double phi = stream().findAny().map(BarSplittingBiasedHistogram::phi).orElseThrow(AssertionError::new);
    double alphaPhi = stream().findAny().map(BarSplittingBiasedHistogram::alphaPhi).orElseThrow(AssertionError::new);

    List<Histogram.Bucket> buckets = new ArrayList<>(bucketCount);
    double targetSize = size() * alphaPhi; // * phi^0
    Iterator<double[]> it = bars.iterator();
    double[] b = it.next();
    double minimum = b[0];
    double count = b[2];
    for (int i = 0; i < bucketCount - 1 && it.hasNext(); i++) {
      while (count < targetSize && it.hasNext()) {
        count += (b = it.next())[2];
      }

      double surplus = count - targetSize;
      double maximum = nextUpIfEqual(minimum, b[1] - ((b[1] - b[0]) * surplus / b[2]));
      buckets.add(new ImmutableBucket(minimum, maximum, targetSize));
      minimum = maximum;
      count = surplus;
      targetSize *= phi;
    }
    while (it.hasNext()) {
      count += (b = it.next())[2];
    }
    buckets.add(new ImmutableBucket(minimum, nextUpIfEqual(minimum, b[1]), count));
    return buckets;
  }

  static void mergeBars(List<double[]> bars) {
    ListIterator<double[]> listIt = bars.listIterator();

    if (listIt.hasNext()) {
      double[] a = listIt.next();
      while (listIt.hasNext()) {
        double[] b = listIt.next();
        if (a[1] > b[0]) {
          listIt.remove();
          listIt.previous();
          listIt.remove();
          int backtrackTo = listIt.nextIndex();
          for (double[] f : flatten(a, b)) {
            while (listIt.hasNext()) {
              double[] next = listIt.next();
              if (f[0] < next[0] || f[0] == next[0] && f[1] < next[1]) {
                listIt.previous();
                break;
              }
            }
            listIt.add(f);
          }
          while (listIt.nextIndex() != backtrackTo) {
            listIt.previous();
          }
          if (listIt.hasNext()) {
            a = listIt.next();
          } else {
            break;
          }
        } else {
          a = b;
        }
      }
    }

  }

  private static List<double[]> flatten(double[] a, double[] b) {
    //each array:
    //array[0] = minimum
    //array[1] = maximum
    //array[2] = count
    double aDensity = a[2] / (a[1] - a[0]);
    double bDensity = b[2] / (b[1] - b[0]);
    if (a[0] < b[0] ) {
      if (a[1] < b[1]) {
        //head(a), tail(a)+head(b), tail(b)
        return asList(
            new double[] {a[0], b[0], (b[0] - a[0]) * aDensity},
            new double[] {b[0], a[1], (a[1] - b[0]) * (aDensity + bDensity)},
            new double[] {a[1], b[1], (b[1] - a[1]) * bDensity});
      } else if (b[1] < a[1]) {
        //head(a), mid(a)+b, tail(a)
        return asList(
            new double[] {a[0], b[0], (b[0] - a[0]) * aDensity},
            new double[] {b[0], b[1], (b[1] - b[0]) * aDensity + b[2]},
            new double[] {b[1], a[1], (a[1] - b[1]) * aDensity});
      } else {
        //head(a), tail(a)+b
        return asList(
            new double[] {a[0], b[0], aDensity * (b[0] - a[0])},
            new double[] {b[0], b[1], aDensity * (b[1] - b[0]) + b[2]});
      }
    } else if (a[0] == b[0]) {
      if (a[1] < b[1]) {
        //a+head(b), tail(b)
        return asList(
            new double[] {a[0], a[1], bDensity * (a[1] - a[0]) + a[2]},
            new double[] {a[1], b[1], bDensity * (b[1] - a[1])});
      } else if (b[1] < a[1]) {
        //b+head(a), tail(a)
        return asList(
            new double[] {b[0], b[1], aDensity * (b[1] - b[0]) + b[2]},
            new double[] {b[1], a[1], aDensity * (a[1] - b[1])});
      } else {
        //a+b
        return asList(new double[] {a[0], a[1], a[2] + b[2]});
      }
    } else {
      //impossible unless list is misordered
      throw new AssertionError();
    }
  }

  @Override
  public double[] getQuantileBounds(double quantile) {
    if (quantile > 1.0 || quantile < 0.0) {
      throw new IllegalArgumentException("Invalid quantile requested: " + quantile);
    } else {
      return of(evaluateQuantileFromMin(quantile), evaluateQuantileFromMax(quantile))
          .min(comparingDouble(bounds -> bounds[1] - bounds[0])).get();
    }
  }

  private double[] evaluateQuantileFromMax(double quantile) {
    double[] sizeBounds = getSizeBounds();
    double lowThreshold = (1.0 - quantile) * sizeBounds[0];
    double highThreshold = (1.0 - quantile) * sizeBounds[1];

    List<double[]> barsByMinimum = stream()
        .flatMap(h -> h.bars().stream().map(bar -> new double[] {bar.minimum(), bar.count() * (1.0 - bar.epsilon())}))
        .sorted(comparingDouble(tuple -> tuple[0])).collect(toList());

    List<double[]> barsByMaximum = stream()
        .flatMap(h -> h.bars().stream().map(bar -> new double[] {bar.maximum(), bar.count() * (1.0 + bar.epsilon())}))
        .sorted(comparingDouble(tuple -> tuple[0])).collect(toList());

    double highCount = 0;
    for (ListIterator<double[]> upperIt = barsByMaximum.listIterator(barsByMaximum.size()); upperIt.hasPrevious(); ) {
      double[] upperB = upperIt.previous();
      highCount += upperB[1];

      if (highCount >= lowThreshold) {
        double lowCount = 0;
        double[] lowerB = null;
        for (ListIterator<double[]> lowerIT = barsByMinimum.listIterator(barsByMinimum.size()); lowerIT.hasPrevious(); ) {
          lowerB = lowerIT.previous();
          lowCount += lowerB[1];

          if (lowCount >= highThreshold) {
            break;
          }
        }
        return new double[] {lowerB[0], upperB[0]};
      }
    }
    throw new AssertionError();
  }

  private double[] evaluateQuantileFromMin(double quantile) {
    double[] sizeBounds = getSizeBounds();
    double lowThreshold = quantile * sizeBounds[0];
    double highThreshold = quantile * sizeBounds[1];

    List<double[]> barsByMinimum = stream()
        .flatMap(h -> h.bars().stream().map(bar -> new double[] {bar.minimum(), bar.count() * (1.0 + bar.epsilon())}))
        .sorted(comparingDouble(tuple -> tuple[0])).collect(toList());
    List<double[]> barsByMaximum = stream()
        .flatMap(h -> h.bars().stream().map(bar -> new double[] {bar.maximum(), bar.count() * (1.0 - bar.epsilon())}))
        .sorted(comparingDouble(tuple -> tuple[0])).collect(toList());

    double highCount = 0;
    for (ListIterator<double[]> lowerIt = barsByMinimum.listIterator(); lowerIt.hasNext(); ) {
      double[] lowerB = lowerIt.next();
      highCount += lowerB[1];

      if (highCount >= lowThreshold) {
        double lowCount = 0;
        double[] upperB = null;
        for (ListIterator<double[]> upperIt = barsByMaximum.listIterator(); upperIt.hasNext(); ) {
          upperB = upperIt.next();
          lowCount += upperB[1];

          if (lowCount >= highThreshold) {
            break;
          }
        }
        return new double[] {lowerB[0], upperB[0]};
      }
    }
    throw new AssertionError();
  }

  @Override
  public long size() {
    return stream().mapToLong(Histogram::size).sum();
  }

  @Override
  public double[] getSizeBounds() {
    return stream().map(BarSplittingBiasedHistogram::getSizeBounds).reduce((a, b) -> {
      a[0] += b[0];
      a[1] += b[1];
      return a;
    }).orElseThrow(AssertionError::new);
  }

  @Override
  public void event(double value, long time) {
    process(h -> h.event(value, time));
  }

  @Override
  public void expire(long time) {
    stream().forEach(h -> h.expire(time));
  }
}
