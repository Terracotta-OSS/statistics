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
package org.terracotta.statistics;

import org.junit.Test;

import java.util.Collections;
import java.util.concurrent.Callable;

import static org.hamcrest.core.IsEqual.*;
import static org.junit.Assert.assertThat;

/**
 *
 * @author henri-tremblay
 */
public class PassThroughStatisticTest {

  private PassThroughStatistic<Integer> stat =
      new PassThroughStatistic<Integer>(this, "name", Collections.singleton("tag"), Collections.singletonMap("key", "value"),
          new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
              return 42;
            }
          });

  @Test
  public void testAttributes() {
    assertThat(stat.name, equalTo("name"));
    assertThat(stat.properties.get("key"), equalTo((Object) "value"));
    assertThat(stat.tags.iterator().next(), equalTo("tag"));
  }

  @Test
  public void testSource() {
    assertThat(stat.value(), equalTo(42));
  }
}
