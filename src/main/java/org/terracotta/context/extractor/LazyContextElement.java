/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context.extractor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.terracotta.context.ContextElement;

class LazyContextElement implements ContextElement {

  public final Class identifier;
  public final Map<? extends String, AttributeGetter<? extends Object>> attributes;

  public LazyContextElement(Class identifier, Map<? extends String, AttributeGetter<? extends Object>> attributes) {
    this.identifier = identifier;
    this.attributes = Collections.unmodifiableMap(new HashMap(attributes));
  }
  
  @Override
  public Class identifier() {
    return identifier;
  }

  @Override
  public Map<String, Object> attributes() {
    Map<String, Object> realized = new HashMap<String, Object>();
    for (Entry<? extends String, AttributeGetter<? extends Object>> e : attributes.entrySet()) {
      realized.put(e.getKey(), e.getValue().get());
    }
    return Collections.unmodifiableMap(realized);
  }
  
  @Override
  public String toString() {
    return identifier() + " " + attributes();
  }
}
