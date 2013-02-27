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
package org.terracotta.statistics.derived;

import java.util.EnumSet;
import java.util.Set;
import org.terracotta.statistics.AbstractSourceStatistic;
import org.terracotta.statistics.jsr166e.ThreadLocalRandom;
import org.terracotta.statistics.observer.ChainedEventObserver;
import org.terracotta.statistics.observer.ChainedOperationObserver;

/**
 *
 * @author cdennis
 */
public class LatencySampling<T extends Enum<T>> extends AbstractSourceStatistic<ChainedEventObserver> implements ChainedOperationObserver<T> {

  private final ThreadLocal<Long> operationStartTime = new ThreadLocal<Long>();
  private final Set<T> targetOperations;
  private final int ceiling;
  
  public LatencySampling(Set<T> targets, double sampling) {
    if (sampling > 1.0 || sampling < 0.0) {
      throw new IllegalArgumentException();
    }
    this.ceiling = (int) (Integer.MAX_VALUE * sampling);
    this.targetOperations = EnumSet.copyOf(targets);
  }

  @Override
  public void begin(long time) {
    if (sample()) {
      operationStartTime.set(time);
    }
  }

  @Override
  public void end(long time, T result) {
    if (targetOperations.contains(result)) {
      Long start  = operationStartTime.get();
      if (start != null) {
        long latency = time - start.longValue();
        if (!derivedStatistics.isEmpty()) {
          for (ChainedEventObserver observer : derivedStatistics) {
            observer.event(time, latency);
          }
        }
      }
    }
    operationStartTime.remove();
  }

  @Override
  public void end(long time, T result, long ... parameters) {
    end(time, result);
  }
  
  private boolean sample() {
    return ceiling == 1.0 || ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE) < ceiling;
  }
}
