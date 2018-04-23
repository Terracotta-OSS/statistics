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
package org.terracotta.statistics;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author cdennis
 */
public class Sample<T extends Serializable> implements Serializable {

  private static final long serialVersionUID = 1L;

  private final long timestamp;
  private final T sample;

  public Sample(long timestamp, T sample) {
    this.sample = sample; // can be null
    this.timestamp = timestamp;
  }

  public T getSample() {
    return sample;
  }

  /**
   * @return The sample time in milliseconds
   */
  public long getTimestamp() {
    return timestamp;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Sample)) return false;
    Sample<?> sample1 = (Sample<?>) o;
    return getTimestamp() == sample1.getTimestamp() && Objects.equals(getSample(), sample1.getSample());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getTimestamp(), getSample());
  }

  @Override
  public String toString() {
    return getSample() + " @ " + getTimestamp();
  }

}
