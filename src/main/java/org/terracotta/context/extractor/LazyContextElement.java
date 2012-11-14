/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context.extractor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.terracotta.context.ContextElement;

class LazyContextElement<I, K, V> implements ContextElement<I, K, V> {

  public final I identifier;
  public final Map<? extends K, AttributeGetter<? extends V>> attributes;

  public LazyContextElement(I identifier, Map<? extends K, AttributeGetter<? extends V>> attributes) {
    this.identifier = identifier;
    this.attributes = Collections.unmodifiableMap(new HashMap(attributes));
  }
  
  @Override
  public I identifier() {
    return identifier;
  }

  @Override
  public Map<K, V> attributes() {
    Map<K, V> realized = new HashMap<K, V>();
    for (Entry<? extends K, AttributeGetter<? extends V>> e : attributes.entrySet()) {
      realized.put(e.getKey(), e.getValue().get());
    }
    return Collections.unmodifiableMap(realized);
  }
  
  @Override
  public String toString() {
    return identifier() + " " + attributes();
  }
}
