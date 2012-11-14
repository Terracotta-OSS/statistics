/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context.extractor;

class DirectAttributeGetter<T> implements AttributeGetter<T> {

  private final T object;
  
  DirectAttributeGetter(T object) {
    this.object = object;
  }

  @Override
  public T get() {
    return object;
  }
}
