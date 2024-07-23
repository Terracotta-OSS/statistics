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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;

public class WeakIdentityHashMap<K, V> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WeakIdentityHashMap.class);

  private final ReferenceQueue<K> referenceQueue = new ReferenceQueue<>();
  private final ConcurrentHashMap<Reference<K>, V> backing = new ConcurrentHashMap<>();

  public V get(K key) {
    clean();
    return backing.get(createReference(key, null));
  }

  public V putIfAbsent(K key, V value) {
    clean();
    return backing.putIfAbsent(createReference(key, referenceQueue), value);
  }

  public V remove(K key) {
    V v = backing.remove(createReference(key, null));
    clean();
    return v;
  }

  private void clean() {
    Reference<? extends K> ref;
    while ((ref = referenceQueue.poll()) != null) {
      V dead = backing.remove(ref);
      if (dead instanceof Cleanable) {
        try {
          ((Cleanable) dead).clean();
        } catch (Throwable t) {
          LOGGER.warn("Cleaning failed with : {}", t);
        }
      }
    }
  }

  protected Reference<K> createReference(K key, ReferenceQueue<? super K> queue) {
    return new IdentityWeakReference<>(key, queue);
  }

  static class IdentityWeakReference<T> extends WeakReference<T> {

    private final int hashCode;

    public IdentityWeakReference(T t) {
      this(t, null);
    }

    public IdentityWeakReference(T t, ReferenceQueue<? super T> rq) {
      super(t, rq);
      this.hashCode = System.identityHashCode(t);
    }


    @Override
    public int hashCode() {
      return hashCode;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      } else if (o instanceof IdentityWeakReference<?>) {
        T ourReferent = get();
        return ourReferent != null && ourReferent == ((IdentityWeakReference<?>) o).get();
      } else {
        return false;
      }
    }
  }

  public interface Cleanable {
    void clean();
  }

}
