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
 * Operation observers track the occurrence of processes which take a finite time
 * and can potential terminate in different ways.
 * <p>
 * Operations must have an associated enum type that represents their possible
 * outcomes.  An example of such an enum type would be:
 * <pre>
 * enum PlaneFlight {
 *   LAND, CRASH;
 * }
 * </pre>
 * Operations also have an associated parameter the use of which is left up to
 * the implementors of both the producer and consumer of events.
 * 
 * @param <T> Enum type representing the possible operations 'results'
 */
public interface OperationObserver<T extends Enum<T>> {
  
  /**
   * Called immediately prior to the operation beginning.
   */
  void begin();
  
  /**
   * Called immediately after the operation completes with no interesting parameters.
   * 
   * @param result the operation result
   */
  void end(T result);
  
  /**
   * Called immediately after the operation completes.
   * 
   * @param result the operation result
   * @param parameters the operation parameters
   */
  void end(T result, long ... parameters);
}
