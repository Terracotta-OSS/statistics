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
  public void testRegularQuantiles() {
    List<Long> values = new ArrayList<Long>();

    Quantile[] selected = new Quantile[] {quantile(0.90, 0.05), quantile(0.95, 0.02), quantile(0.99, 0.001)};
    TargetedQuantiles quantiles = new TargetedQuantiles(selected);
    long seed = System.nanoTime();
    System.out.println("testRegularQuantiles : seed=" + seed);
    Random random = new Random(seed);
    for (int i = 0; i < 10000; i++) {
      long value = (long) (random.nextGaussian() * 10000L);
      quantiles.event(value);
      values.add(value);
      
      Collections.sort(values);

      for (Quantile quantile : selected) {
        verifyQuantile(values, quantile, quantiles);
      }
    }
  }
  
  @Test
  public void testVeryLargeErrorQuantile() {
    List<Long> values = new ArrayList<Long>();

    Quantile[] selected = new Quantile[] {quantile(0.90, 0.2)};
    TargetedQuantiles quantiles = new TargetedQuantiles(selected);
    long seed = System.nanoTime();
    System.out.println("testVeryLargeErrorQuantile : seed=" + seed);
    Random random = new Random(seed);
    for (int i = 0; i < 10000; i++) {
      long value = (long) (random.nextGaussian() * 10000L);
      quantiles.event(value);
      values.add(value);

      Collections.sort(values);

      for (Quantile quantile : selected) {
        verifyQuantile(values, quantile, quantiles);
      }
    }
  }
  
  @Test
  public void testLowQuantiles() {
    List<Long> values = new ArrayList<Long>();

    Quantile[] selected = new Quantile[] {quantile(0.10, 0.05)};
    TargetedQuantiles quantiles = new TargetedQuantiles(selected);
    long seed = System.nanoTime();
    System.out.println("testLowQuantiles : seed=" + seed);
    Random random = new Random(seed);
    for (int i = 0; i < 10000; i++) {
      long value = (long) (random.nextGaussian() * 10000L);
      quantiles.event(value);
      values.add(value);
      
      Collections.sort(values);

      for (Quantile quantile : selected) {
        verifyQuantile(values, quantile, quantiles);
      }
    }
  }
  
  private static void verifyQuantile(List<Long> values, Quantile quantile, TargetedQuantiles quantiles) {
      long sampled = quantiles.quantile(quantile.quantile());
      long floor = values.get(Math.max(0, (int) Math.floor((quantile.quantile() - quantile.error()) * values.size())));
      long ceiling = values.get(Math.min(values.size() - 1, (int) Math.ceil((quantile.quantile() + quantile.error()) * values.size())));
      assertThat(quantiles.toString(), sampled, both(greaterThanOrEqualTo(floor)).and(lessThanOrEqualTo(ceiling)));
  }
}
