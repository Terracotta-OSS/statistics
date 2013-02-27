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
 * Event observers track the occurrence of singular events.
 * <p>
 * Events can have an associated parameter the use of which is left up to
 * the implementors of both the producer and consumer of events.
 */
public interface ChainedEventObserver extends ChainedObserver {
  
  /*
   * The parameter argument is really a constrained and collapsed to primitive
   * generic type with no bounds.  So the "real" class declaration should be:
   * @{code interface EventObserver<T>}
   */
  
  /**
   * Called to indicate an event happened.
   * 
   * @param parameter the event parameter
   */
  void event(long time, long ... parameters);
}
