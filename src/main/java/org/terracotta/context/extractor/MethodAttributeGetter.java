/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context.extractor;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class MethodAttributeGetter<T> implements AttributeGetter<T> {

  private final WeakReference<Object> reference;
  private final Method method;
  
  MethodAttributeGetter(Object object, Method method) {
    this.reference = new WeakReference<Object>(object);
    this.method = method;
  }

  @Override
  public T get() {
    try {
      Object target = reference.get();
      return (T) method.invoke(target);
    } catch (IllegalAccessException ex) {
      throw new RuntimeException(ex);
    } catch (IllegalArgumentException ex) {
      throw new RuntimeException(ex);
    } catch (InvocationTargetException ex) {
      throw new RuntimeException(ex);
    }
  }
}
