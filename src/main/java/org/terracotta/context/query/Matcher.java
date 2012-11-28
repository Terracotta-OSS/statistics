/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context.query;

import java.lang.reflect.Method;

public abstract class Matcher<T> {
  
  private Class<? extends T> boundType = (Class<? extends T>) getSafeType(getClass());
   
  public static Class<?> getSafeType(Class<?> fromClass) {
    for (Class<?> c = fromClass; c != Object.class; c = c.getSuperclass()) {
        for (Method method : c.getDeclaredMethods()) {
            if (method.getName().equals("matchesSafely")
              && method.getParameterTypes().length == 1
              && !method.isSynthetic()) {
                return method.getParameterTypes()[0];
            }
        }
    }
    throw new AssertionError();
  }

  public final boolean matches(Object object) {
    if (boundType.isAssignableFrom(object.getClass())) {
      return matchesSafely(boundType.cast(object));
    } else {
      return false;
    }
  }
  
  abstract boolean matchesSafely(T object);
}
