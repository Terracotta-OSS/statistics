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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.terracotta.context.ContextElement;
import org.terracotta.context.annotations.ContextAttribute;

/**
 * Extracts context information from object instances and creates appropriate 
 * enclosing {@link ContextElement} instances.
 */
public final class ObjectContextExtractor {

  private ObjectContextExtractor() {
    //singleton
  }
  
  /**
   * Returns a {@code ContextElement} instance for the supplied object.
   * <p>
   * The supplied object's class type is parsed for {@link ContextAttribute} 
   * annotations and the associated attributes are extracted and returned in the
   * form of a {@code ContextElement}.
   * 
   * @param from object to extract context for
   * @return a {@code ContextElement}
   */
  public static ContextElement extract(Object from) {
    Map<String, AttributeGetter<Object>> attributes = new HashMap<>();
    attributes.putAll(extractInstanceAttribute(from));
    attributes.putAll(extractMethodAttributes(from));
    attributes.putAll(extractFieldAttributes(from));
    return new LazyContextElement(from.getClass(), attributes);
  }

  private static Map<? extends String, ? extends AttributeGetter<Object>> extractInstanceAttribute(Object from) {
    ContextAttribute annotation = from.getClass().getAnnotation(ContextAttribute.class);
    if (annotation == null) {
      return Collections.emptyMap();
    } else {
      return Collections.singletonMap(annotation.value(), new WeakAttributeGetter<>(from));
    }
  }

  private static Map<String, AttributeGetter<Object>> extractMethodAttributes(Object from) {
    Map<String, AttributeGetter<Object>> attributes = new HashMap<>();
    
    for (Method m : from.getClass().getMethods()) {
      if (m.getParameterTypes().length == 0 && m.getReturnType() != Void.TYPE) {
        ContextAttribute annotation = m.getAnnotation(ContextAttribute.class);
        if (annotation != null) {
          attributes.put(annotation.value(), new WeakMethodAttributeGetter<>(from, m));
        }
      }
    }
    return attributes;
  }

  private static Map<String, AttributeGetter<Object>> extractFieldAttributes(Object from) {
    Map<String, AttributeGetter<Object>> attributes = new HashMap<>();
    
    for (Class c = from.getClass(); c != null; c = c.getSuperclass()) {
      for (Field f : c.getDeclaredFields()) {
        ContextAttribute annotation = f.getAnnotation(ContextAttribute.class);
        if (annotation != null) {
          attributes.put(annotation.value(), createFieldAttributeGetter(from, f));
        }
      }
    }
    
    return attributes;
  }

  private static AttributeGetter<Object> createFieldAttributeGetter(Object from, Field f) {
    f.setAccessible(true);
    if (Modifier.isFinal(f.getModifiers())) {
      try {
        return new DirectAttributeGetter<>(f.get(from));
      } catch (IllegalArgumentException | IllegalAccessException ex) {
        throw new RuntimeException(ex);
      }
    } else {
      return new WeakFieldAttributeGetter<>(from, f);
    }
  }
}
