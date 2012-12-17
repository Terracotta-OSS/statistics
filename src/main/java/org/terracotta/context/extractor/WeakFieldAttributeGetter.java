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

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

/**
 *
 * @author cdennis
 */
public class WeakFieldAttributeGetter<T> extends FieldAttributeGetter<T> {

  private final WeakReference<Object> targetRef;
  
  public WeakFieldAttributeGetter(Object target, Field field) {
    super(field);
    this.targetRef = new WeakReference<Object>(target);
  }

  @Override
  Object target() {
    return targetRef.get();
  }
}
