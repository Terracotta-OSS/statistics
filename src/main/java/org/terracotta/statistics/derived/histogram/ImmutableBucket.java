/*
 * Copyright Terracotta, Inc.
 * Copyright Super iPaaS Integration LLC, an IBM Company.
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

class ImmutableBucket implements Histogram.Bucket {

  private final double minimum;
  private final double maximum;
  private final double count;

  ImmutableBucket(double minimum, double maximum, double count) {
    this.minimum = minimum;
    this.maximum = maximum;
    this.count = count;
  }

  @Override
  public double minimum() {
    return minimum;
  }

  @Override
  public double maximum() {
    return maximum;
  }

  @Override
  public double count() {
    return count;
  }

  @Override
  public String toString() {
    return "[min=" + minimum() + " (count=" + count() + ", height=" + (count() / (maximum() - minimum())) + ") max=" + maximum() + "]";
  }

}
