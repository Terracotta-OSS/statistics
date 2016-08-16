/*
 * Copyright Terracotta, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ehcache.clustered.client.internal.lock;

import java.io.Closeable;

import org.ehcache.clustered.common.internal.lock.LockMessaging.HoldType;
import org.terracotta.connection.Connection;
import org.terracotta.connection.entity.EntityRef;
import org.terracotta.exception.EntityAlreadyExistsException;
import org.terracotta.exception.EntityNotFoundException;
import org.terracotta.exception.EntityNotProvidedException;
import org.terracotta.exception.EntityVersionMismatchException;

public class VoltronReadWriteLock {

  private final EntityRef<VoltronReadWriteLockClient, Void> reference;

  public VoltronReadWriteLock(Connection connection, String id) {
    try {
      this.reference = createEntityRef(connection, id);
    } catch (EntityNotProvidedException e) {
      throw new IllegalStateException(e);
    }
  }

  public Hold readLock() {
    return lock(HoldType.READ);
  }

  public Hold writeLock() {
    return lock(HoldType.WRITE);
  }

  public Hold tryReadLock() {
    return tryLock(HoldType.READ);
  }

  public Hold tryWriteLock() {
    return tryLock(HoldType.WRITE);
  }

  private Hold lock(final HoldType type) {
    final VoltronReadWriteLockClient client = createClientEntity();
    client.lock(type);
    return new HoldImpl(client, type);
  }

  private Hold tryLock(final HoldType type) {
    final VoltronReadWriteLockClient client = createClientEntity();
    if (client.tryLock(type)) {
      return new HoldImpl(client, type);
    } else {
      client.close();
      tryDestroy();
      return null;
    }
  }

  private boolean tryDestroy() {
    try {
      return reference.tryDestroy();
    } catch (EntityNotProvidedException e) {
      throw new AssertionError(e);
    } catch (EntityNotFoundException e) {
      return false;
    }
  }

  public interface Hold extends Closeable {

    @Override
    void close();

    void unlock();

  }

  private class HoldImpl implements Hold {

    private final VoltronReadWriteLockClient client;
    private final HoldType type;

    public HoldImpl(VoltronReadWriteLockClient client, HoldType type) {
      this.client = client;
      this.type = type;
    }

    @Override
    public void close() {
      unlock();
    }

    @Override
    public void unlock() {
      client.unlock(type);
      client.close();
      tryDestroy();
    }
  }

  private VoltronReadWriteLockClient createClientEntity() {
    try {
      while (true) {
        try {
          reference.create(null);
        } catch (EntityAlreadyExistsException f) {
          //ignore
        }
        try {
          return reference.fetchEntity();
        } catch (EntityNotFoundException e) {
          //ignore
        }
      }
    } catch (EntityVersionMismatchException e) {
      throw new IllegalStateException(e);
    } catch (EntityNotProvidedException e) {
      throw new IllegalStateException(e);
    }
  }

  private static EntityRef<VoltronReadWriteLockClient, Void> createEntityRef(Connection connection, String identifier) throws EntityNotProvidedException {
    return connection.getEntityRef(VoltronReadWriteLockClient.class, 1, "VoltronReadWriteLock-" + identifier);
  }
}
