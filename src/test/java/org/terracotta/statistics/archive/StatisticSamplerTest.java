/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.statistics.archive;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.terracotta.statistics.ValueStatistic;

/**
 *
 * @author cdennis
 */
public class StatisticSamplerTest {
  
  @Test
  public void testUnstartedSampler() {
    StatisticSampler<String> sampler = new StatisticSampler<String>(1L, TimeUnit.NANOSECONDS, new ValueStatistic<String>() {

      @Override
      public String value() {
        throw new AssertionError();
      }
    }, DevNull.DEV_NULL);
    
    sampler.shutdown();
  }
}
