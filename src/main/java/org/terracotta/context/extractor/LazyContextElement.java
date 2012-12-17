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
    this.attributes = new HashMap(attributes);
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
