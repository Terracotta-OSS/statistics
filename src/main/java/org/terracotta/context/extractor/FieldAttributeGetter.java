/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context.extractor;

import java.lang.reflect.Field;

class FieldAttributeGetter<T> implements AttributeGetter<T> {

  private final Object object;
  private final Field field;
  
  FieldAttributeGetter(Object object, Field field) {
    this.object = object;
    this.field = field;
  }

  @Override
  public T get() {
    try {
      return (T) field.get(object);
    } catch (IllegalArgumentException ex) {
      throw new RuntimeException(ex);
    } catch (IllegalAccessException ex) {
      throw new RuntimeException(ex);
    }
  }
}
