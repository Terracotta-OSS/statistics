/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.terracotta.statistics.derived;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.terracotta.statistics.derived.TargetedQuantiles.Quantile;

import static org.hamcrest.core.CombinableMatcher.*;
import static org.hamcrest.number.OrderingComparison.*;
import static org.junit.Assert.*;
import static org.terracotta.statistics.derived.TargetedQuantiles.quantile;
/**
 *
 * @author cdennis
 */
public class TargetedQuantilesTest {
  
  @Test
  public void testTargetedQuantiles() {
    List<Long> values = new ArrayList<Long>();

    Quantile[] selected = new Quantile[] {quantile(0.90, 0.05), quantile(0.95, 0.02), quantile(0.99, 0.01)};
    TargetedQuantiles quantiles = new TargetedQuantiles(selected);
    long seed = System.nanoTime();
    System.out.println("testTargetedQuantiles : seed=" + seed);
    Random random = new Random(seed);
    for (int i = 0; i < 10000; i++) {
      long value = (long) (random.nextGaussian() * 10000L);
      quantiles.event(value);
      values.add(value);
    }
    
    Collections.sort(values);
    
    for (Quantile quantile : selected) {
      long sampled = quantiles.quantile(quantile.quantile());
      long floor = values.get(Math.max(0, (int) Math.floor((1 - quantile.error()) * quantile.quantile() * values.size())));
      long ceiling = values.get(Math.min(values.size() - 1, (int) Math.ceil((1 + quantile.error()) * quantile.quantile() * values.size())));
      assertThat(sampled, both(greaterThanOrEqualTo(floor)).and(lessThanOrEqualTo(ceiling)));
    }
  }
}
