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
package org.terracotta.statistics.simulation.latency;

import java.io.Serializable;

/**
 * @author Mathieu Carbou
 */
public class LongSample implements Serializable, Comparable<LongSample> {

  private static final long serialVersionUID = 1L;

  private final long time;
  private final long value;

  private LongSample(long time, long value) {
    this.time = time;
    this.value = value;
  }

  public long value() {
    return value;
  }

  public long time() {
    return time;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    LongSample that = (LongSample) o;
    if (time != that.time) return false;
    return value == that.value;
  }

  @Override
  public int hashCode() {
    int result = (int) (time ^ (time >>> 32));
    result = 31 * result + (int) (value ^ (value >>> 32));
    return result;
  }

  @Override
  public String toString() {
    return value + "@" + time;
  }

  @Override
  public int compareTo(LongSample o) {
    return Long.compare(time, o.time);
  }

  public static LongSample sample(long time, long value) {
    return new LongSample(time, value);
  }
}
