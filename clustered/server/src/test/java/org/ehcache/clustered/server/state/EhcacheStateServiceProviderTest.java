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

package org.ehcache.clustered.server.state;

import org.ehcache.clustered.server.state.config.EhcacheStateServiceConfig;
import org.junit.Test;
import org.terracotta.entity.ServiceConfiguration;
import org.terracotta.entity.ServiceProviderCleanupException;
import org.terracotta.entity.ServiceProviderConfiguration;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class EhcacheStateServiceProviderTest {

  @Test
  public void testInitialize() {
    EhcacheStateServiceProvider serviceProvider = new EhcacheStateServiceProvider();

    ServiceProviderConfiguration serviceProviderConfiguration = mock(ServiceProviderConfiguration.class);

    assertTrue(serviceProvider.initialize(serviceProviderConfiguration));
  }

  @Test
  public void testGetService() {
    EhcacheStateServiceProvider serviceProvider = new EhcacheStateServiceProvider();

    EhcacheStateService ehcacheStateService = serviceProvider.getService(1L, new EhcacheStateServiceConfig(null, null));

    assertNotNull(ehcacheStateService);

    EhcacheStateService sameStateService = serviceProvider.getService(1L, new EhcacheStateServiceConfig(null, null));

    assertSame(ehcacheStateService, sameStateService);

    EhcacheStateService anotherStateService = serviceProvider.getService(2L, new EhcacheStateServiceConfig(null, null));

    assertNotNull(anotherStateService);
    assertNotSame(ehcacheStateService, anotherStateService);

  }

  @Test
  public void testClear() throws ServiceProviderCleanupException {
    EhcacheStateServiceProvider serviceProvider = new EhcacheStateServiceProvider();

    EhcacheStateService ehcacheStateService = serviceProvider.getService(1L, new EhcacheStateServiceConfig(null, null));
    EhcacheStateService anotherStateService = serviceProvider.getService(2L, new EhcacheStateServiceConfig(null, null));

    serviceProvider.clear();

    EhcacheStateService ehcacheStateServiceAfterClear = serviceProvider.getService(1L, new EhcacheStateServiceConfig(null, null));
    EhcacheStateService anotherStateServiceAfterClear = serviceProvider.getService(2L, new EhcacheStateServiceConfig(null, null));

    assertNotSame(ehcacheStateService, ehcacheStateServiceAfterClear);
    assertNotSame(anotherStateService, anotherStateServiceAfterClear);
  }

}
