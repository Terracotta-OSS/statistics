/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context.extractor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.terracotta.context.ContextElement;
import org.terracotta.context.annotations.ContextAttribute;

public final class ObjectContextExtractor {

  private ObjectContextExtractor() {
    //singleton
  }
  
  public static ContextElement<Class, String, Object> extract(Object from) {
    Map<String, AttributeGetter<? extends Object>> attributes = new HashMap<String, AttributeGetter<? extends Object>>();
    if (from.getClass().isAnnotationPresent(ContextAttribute.class)) {
      attributes.put(from.getClass().getAnnotation(ContextAttribute.class).value(), new WeakAttributeGetter<Object>(from));
    }
    attributes.putAll(extractMethodAttributes(from));
    attributes.putAll(extractFieldAttributes(from));
    return new LazyContextElement<Class, String, Object> (from.getClass(), attributes);
  }

  private static Map<String, AttributeGetter<? extends Object>> extractMethodAttributes(Object from) {
    Map<String, AttributeGetter<? extends Object>> attributes = new HashMap<String, AttributeGetter<? extends Object>>();
    
    for (Method m : from.getClass().getMethods()) {
      if (m.getParameterTypes().length == 0 && m.getReturnType() != Void.TYPE && m.isAnnotationPresent(ContextAttribute.class)) {
        m.setAccessible(true);
        attributes.put(m.getAnnotation(ContextAttribute.class).value(), new MethodAttributeGetter(from, m));
      }
    }
    
    return attributes;
  }

  private static Map<String, AttributeGetter<? extends Object>> extractFieldAttributes(Object from) {
    Map<String, AttributeGetter<? extends Object>> attributes = new HashMap<String, AttributeGetter<? extends Object>>();
    
    for (Class c = from.getClass(); c != null; c = c.getSuperclass()) {
      for (Field f : c.getDeclaredFields()) {
        if (f.isAnnotationPresent(ContextAttribute.class)) {
          attributes.put(f.getAnnotation(ContextAttribute.class).value(), createFieldAttributeGetter(from, f));
        }
      }
    }
    
    return attributes;
  }

  private static AttributeGetter<? extends Object> createFieldAttributeGetter(Object from, Field f) {
    f.setAccessible(true);
    if (Modifier.isFinal(f.getModifiers())) {
      try {
        return new DirectAttributeGetter(f.get(from));
      } catch (IllegalArgumentException ex) {
        throw new RuntimeException(ex);
      } catch (IllegalAccessException ex) {
        throw new RuntimeException(ex);
      }
    } else {
      return new FieldAttributeGetter(from, f);
    }
  }
}
