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
package org.terracotta.statistics.observer;

/**
 * The marker interface implemented by all statistic observer classes.
 * <p>
 * A statistic observer presents methods used to update a statistic.  There are
 * two general classes of observer implementations:
 * <ol>
 *   <li>Initial observers are called by product code in order to record the
 * occurrence of a product related 'event'</li>
 *   <li>Derived observers are called by other observers with then intention of
 * tracking higher order statistics.</li>
 * </ol>
 */
public interface ChainedObserver {
  
}
