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
package org.terracotta.context;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;

public class WeakIdentityHashMap<K, V> {

  private final ReferenceQueue<K> referenceQueue = new ReferenceQueue<K>();
  private final ConcurrentHashMap<Reference<K>, V> backing = new ConcurrentHashMap<Reference<K>, V>();

  public V get(K key) {
    clean();
    return backing.get(new IdentityWeakReference<K>(key));
  }

  public V putIfAbsent(K key, V value) {
    clean();
    return backing.putIfAbsent(new IdentityWeakReference<K>(key, referenceQueue), value);
  }

  private void clean() {
    Reference<? extends K> ref;
    while ((ref = referenceQueue.poll()) != null) {
      backing.remove(ref);
    }
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
}
