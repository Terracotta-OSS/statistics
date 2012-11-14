/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context.util;

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
