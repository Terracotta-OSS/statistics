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

import static org.hamcrest.core.Is.is;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.assertThat;
import org.junit.Test;

import java.util.Random;

/*
 * Selected test cases extracted from <a href="http://citeseerx.ist.psu.edu/viewdoc/citations?doi=10.1.1.225.1196">
 * Fast and Accurate Computation of Equi-Depth Histograms over Data Streams</a>.
 */
public class ExponentialHistogramTest {

  @Test
  public void testPerformance() {
    ExponentialHistogram eh = new ExponentialHistogram(0.7f, 7100);
    long start = System.nanoTime();
    for (int i = 0; i < 1000000; i++) {
      eh.insert(i);
    }
    long last = System.nanoTime() - start;
    System.out.println(((double) last) / 1000000);
  }

  @Test
  public void testMousaviZanioloCounting() {
    ExponentialHistogram eh = new ExponentialHistogram(0.5f, 35);
    inject(eh, 16, 25);
    inject(eh, 8, 34);
    inject(eh, 4, 39);
    inject(eh, 4, 43);
    inject(eh, 2, 48);
    inject(eh, 2, 51);
    inject(eh, 1, 53);
    inject(eh, 1, 55);
    assertThat(eh.count(), is(30L));
    assertThat(eh.toString(), is("count = 30 : [1@55], [1@53], [2@51], [2@48], [4@43], [4@39], [8@34], [16@25]"));

    eh.insert(58);
    eh.expire(58);
    assertThat(eh.count(), is(31L));
    assertThat(eh.toString(), is("count = 31 : [1@58], [2@55], [4@51], [8@43], [8@34], [16@25]"));
    ;

    eh.insert(60);
    eh.expire(60);
    assertThat(eh.count(), is(20L));
    assertThat(eh.toString(), is("count = 20 : [1@60], [1@58], [2@55], [4@51], [8@43], [8@34]"));
  }

  @Test
  public void testMousaviZanioloMerging() {
    ExponentialHistogram ehl = new ExponentialHistogram(0.5f, Long.MAX_VALUE);
    inject(ehl, 4, 39);
    inject(ehl, 4, 48);
    inject(ehl, 2, 52);
    inject(ehl, 1, 53);
    inject(ehl, 1, 55);
    assertThat(ehl.count(), is(10L));
    assertThat(ehl.toString(), is("count = 10 : [1@55], [1@53], [2@52], [4@48], [4@39]"));

    ExponentialHistogram ehr = new ExponentialHistogram(0.5f, Long.MAX_VALUE);
    inject(ehr, 4, 13);
    inject(ehr, 4, 25);
    inject(ehr, 2, 29);
    inject(ehr, 2, 32);
    inject(ehr, 1, 50);
    inject(ehr, 1, 56);
    inject(ehr, 1, 58);
    assertThat(ehr.count(), is(11L));
    assertThat(ehr.toString(), is("count = 11 : [1@58], [2@56], [4@32], [8@25]"));

    ehl.merge(ehr);
    assertThat(ehl.count(), is(23L));
    assertThat(ehl.toString(), is("count = 23 : [1@58], [2@56], [4@55], [4@48], [8@39], [8@25]"));
  }

  @Test
  public void testBulkInsertion() {
    ExponentialHistogram ehA = new ExponentialHistogram(0.1f, Long.MAX_VALUE);
    ExponentialHistogram ehB = new ExponentialHistogram(0.1f, Long.MAX_VALUE);

    long seed = System.nanoTime();
    Random rndm = new Random(seed);
    try {
      int total = 0;
      for (int i = 0; i < 100; i++) {
        int count = rndm.nextInt(1000);
        total += count;
        inject(ehA, count, 0);
        ehB.insert(0, count);

        assertThat(ehB.count(), is(ehA.count()));
        assertThat((double) ehB.count(), closeTo(total, 0.1 * total));
      }
    } catch (Throwable t) {
      System.out.println("Failed Seed : " + seed);
      throw t;
    }
  }

  @Test
  public void testReallyBulkInsertion() {
    long seed = System.nanoTime();
    Random rndm = new Random(seed);

    float accuracy = (rndm.nextFloat() / 2) + 0.1f;
    int window = rndm.nextInt(500) + 10;

    ExponentialHistogram eh = new ExponentialHistogram(accuracy, window);

    try {
      final int max = 1000;
      long total = 0;
      for (int i = 0; i < max; i++) {
        long count = rndm.nextLong() & ((1L << 48) - 1);
        if (max - i <= window) {
          total += count;
        }
        eh.insert(i, count);
      }
      eh.expire(999);
      assertThat((double) eh.count(), closeTo(total, accuracy * total));
    } catch (Throwable t) {
      throw new AssertionError("Failed seed : " + seed, t);
    }
  }

  private static void inject(ExponentialHistogram eh, int count, int before) {
    for (int i = 0; i < count; i++) {
      eh.insert(before);
    }
  }
}
