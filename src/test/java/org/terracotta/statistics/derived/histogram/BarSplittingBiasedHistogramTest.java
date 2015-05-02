/*
 * Copyright 2015 Terracotta, Inc..
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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.terracotta.statistics.derived.histogram.Histogram.Bucket;

/**
 *
 * @author cdennis
 */
public class BarSplittingBiasedHistogramTest {
  
  @Test
  public void test() throws IOException {
    BarSplittingBiasedHistogram bsbh = new BarSplittingBiasedHistogram(0.75f, 20, 1000000);
    long last = 3000L;
    for (int i = 0; i < 2000000; i++) {
      long start = System.nanoTime();
      bsbh.event(last, i);
      last = System.nanoTime() - start;
    }
    List<Bucket<Double>> buckets = bsbh.getBuckets();
    System.out.println("BUCKETS");
    System.out.println(buckets.get(0).minimum() + ",0.0");
    for (Bucket<Double> b : buckets) {
      System.out.println(b.minimum() + "," + b.count() / (b.maximum() - b.minimum()));
    }
    System.out.println(buckets.get(buckets.size() - 1).maximum() + ",0.0");
    
    System.out.println("Minimum " + Arrays.toString(bsbh.getQuantileBounds(0)));
    System.out.println("Median " + Arrays.toString(bsbh.getQuantileBounds(0.5)));
    System.out.println("90%ile " + Arrays.toString(bsbh.getQuantileBounds(0.9)));
    System.out.println("95%ile " + Arrays.toString(bsbh.getQuantileBounds(0.95)));
    System.out.println("99%ile " + Arrays.toString(bsbh.getQuantileBounds(0.99)));
    System.out.println("99.9%ile " + Arrays.toString(bsbh.getQuantileBounds(0.999)));
    System.out.println("99.99%ile " + Arrays.toString(bsbh.getQuantileBounds(0.9999)));
    System.out.println("99.999%ile " + Arrays.toString(bsbh.getQuantileBounds(0.99999)));
    System.out.println("Maximum " + Arrays.toString(bsbh.getQuantileBounds(1.0)));
  }
}
