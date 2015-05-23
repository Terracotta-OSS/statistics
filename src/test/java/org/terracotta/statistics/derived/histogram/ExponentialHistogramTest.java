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
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 *
 * @author cdennis
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
    assertThat(eh.toString(), is("count = 31 : [1@58], [1@55], [1@53], [2@51], [2@48], [4@43], [4@39], [8@34], [16@25]"));

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
    assertThat(ehr.count(), is(13L));
    assertThat(ehr.toString(), is("count = 13 : [1@58], [1@56], [1@50], [2@32], [2@29], [4@25], [4@13]"));
    
    ehl.merge(ehr);
    assertThat(ehl.count(), is(23L));
    assertThat(ehl.toString(), is("count = 23 : [1@58], [1@56], [1@55], [2@53], [2@52], [4@48], [8@39], [8@25]"));
  }
  
  private static void inject(ExponentialHistogram eh, int count, int before) {
    for (int i = 0; i < count; i++) {
      eh.insert(before);
    }
  }
}
