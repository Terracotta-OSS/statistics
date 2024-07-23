/*
 * Copyright Terracotta, Inc.
 * Copyright Super iPaaS Integration LLC, an IBM Company 2024
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

import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public abstract class HistogramPerformanceTest {

  @Test
  public void testNanoTime() throws IOException {
    long total = 0L;
    for (int i = 0; i < 2000000; i++) {
      long start = System.nanoTime();
      total += System.nanoTime() - start;
    }
    System.out.println("System.nanoTime() mean time (ns): " + (((double) total) / 2000000));
  }

  @Test
  public void testSelf() throws IOException, InterruptedException {
    Histogram histogram = selfTime(0.75, 20);

    System.out.println("Minimum " + Arrays.toString(histogram.getQuantileBounds(0)));
    System.out.println("Median " + Arrays.toString(histogram.getQuantileBounds(0.5)));
    System.out.println("90%ile " + Arrays.toString(histogram.getQuantileBounds(0.9)));
    System.out.println("95%ile " + Arrays.toString(histogram.getQuantileBounds(0.95)));
    System.out.println("99%ile " + Arrays.toString(histogram.getQuantileBounds(0.99)));
    System.out.println("99.9%ile " + Arrays.toString(histogram.getQuantileBounds(0.999)));
    System.out.println("99.99%ile " + Arrays.toString(histogram.getQuantileBounds(0.9999)));
    System.out.println("99.999%ile " + Arrays.toString(histogram.getQuantileBounds(0.99999)));
    System.out.println("Maximum " + Arrays.toString(histogram.getQuantileBounds(1.0)));
  }

  protected abstract Histogram selfTime(double bias, int bars) throws InterruptedException;
}
