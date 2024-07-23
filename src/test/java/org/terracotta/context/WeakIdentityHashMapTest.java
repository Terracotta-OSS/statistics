/*
 * Copyright Terracotta, Inc.
 * Copyright Super iPaaS Integration LLC, an IBM Company 2024
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
package org.terracotta.context;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.LinkedList;
import java.util.Queue;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

/**
 *
 * @author cdennis
 */
public class WeakIdentityHashMapTest {
  
  @Test
  public void testEnqueuedReferenceIsRemoved() {
    
    final Queue<Reference<String>> references = new LinkedList<Reference<String>>();
    WeakIdentityHashMap<String, String> map = new WeakIdentityHashMap<String, String>() {
      @Override
      protected Reference<String> createReference(String key, ReferenceQueue<? super String> queue) {
        if (queue == null) {
          return super.createReference(key, queue);
        } else {
          Reference<String> ref = super.createReference(key, queue);
          references.add(ref);
          return ref;
        }
      }
    };
    
    assertThat(map.putIfAbsent("test", "test"), nullValue());
    assertThat(map.get("test"), is("test"));
    
    references.remove().enqueue();
    
    assertThat(map.get("test"), nullValue());
  }

  @Test
  public void testEnqueuedCleanableReferenceIsRemovedAndCleaned() {
    final Queue<Reference<String>> references = new LinkedList<Reference<String>>();
    WeakIdentityHashMap<String, DummyCleanable> map = new WeakIdentityHashMap<String, DummyCleanable>() {
      @Override
      protected Reference<String> createReference(String key, ReferenceQueue<? super String> queue) {
        if (queue == null) {
          return super.createReference(key, queue);
        } else {
          Reference<String> ref = super.createReference(key, queue);
          references.add(ref);
          return ref;
        }
      }
    };
    
    DummyCleanable value = new DummyCleanable();
    assertThat(map.putIfAbsent("test", value), nullValue());
    assertThat(map.get("test"), is(value));
    
    references.remove().enqueue();
    
    assertThat(map.get("test"), nullValue());
    assertThat(value.isClean(), is(true));
  }
  
  static class DummyCleanable implements WeakIdentityHashMap.Cleanable {

    private boolean cleaned = false;
    
    @Override
    public void clean() {
      cleaned = true;
    }

    public boolean isClean() {
      return cleaned;
    }
  }
}
