/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context;

import java.util.Map;

public interface ContextElement<T, K, V> {
  
  T identifier();
  
  Map<K, V> attributes();
}
