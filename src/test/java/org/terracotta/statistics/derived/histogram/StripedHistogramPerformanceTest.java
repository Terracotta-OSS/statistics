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
import org.terracotta.statistics.derived.histogram.StripedHistogram;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAdder;

@Ignore
public class StripedHistogramPerformanceTest extends HistogramPerformanceTest {

  @Override
  protected Histogram selfTime(double bias, int bars) throws InterruptedException {
    StripedHistogram histogram = new StripedHistogram(0.75, 20, 1000000);
    ExecutorService executorService = Executors.newCachedThreadPool();
    try {
      executorService.invokeAll(Collections.nCopies(8, (Callable<Void>) () -> {
        long last = 3000L;
        for (int i = 0; i < 2000000; i++) {
          long start = System.nanoTime();
          histogram.event(last, i);
          last = System.nanoTime() - start;
        }
        return null;
      }));
    } finally {
      executorService.shutdown();
    }
    return histogram;
  }

  @Test
  public void testData() throws IOException, InterruptedException {
    StripedHistogram bsbh = new StripedHistogram(0.75, 20, 1000000);

    Random rndm = new Random();
    long[] data = new long[2000000];
    for (int i = 0; i < data.length; i++) {
      data[i] = (long) (Math.abs(rndm.nextGaussian()) * 3000L);
    }

    ExecutorService executorService = Executors.newCachedThreadPool();
    try {
      executorService.invokeAll(Collections.nCopies(8, (Callable<Void>) () -> {
        long fullStart = System.nanoTime();
        final int cycles = 10;
        for (int c = 0; c < cycles; c++) {
          long start = System.nanoTime();
          for (int i = 0; i < data.length; i++) {
            bsbh.event(data[i], i);
          }
          long total = System.nanoTime() - start;
          System.out.println("\t" + Thread.currentThread() + " Run:" + c + " Mean Time (ns): " + ((double) total) / data.length);
        }
        long fullEnd = System.nanoTime() - fullStart;
        System.out.println(Thread.currentThread() + " Mean Time (ns): " + ((double) fullEnd) / (cycles * data.length));
        return null;
      }));
    } finally {
      executorService.shutdown();
    }

  }
}
