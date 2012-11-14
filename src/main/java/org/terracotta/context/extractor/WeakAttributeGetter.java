/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context.extractor;

import java.lang.ref.WeakReference;

class WeakAttributeGetter<T> implements AttributeGetter<T> {

  private final WeakReference<T> reference;
  
  public WeakAttributeGetter(T object) {
    this.reference = new WeakReference<T>(object);
  }

  @Override
  public T get() {
    return reference.get();
  }
}
