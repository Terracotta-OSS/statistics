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
package org.terracotta.context.extended;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.terracotta.statistics.extended.StatisticType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class OperationStatisticDescriptorTest {

  @Test
  public void getTags_empty() throws Exception {
    OperationStatisticDescriptor<StatisticType> desc = OperationStatisticDescriptor.descriptor("observer", StatisticType.class);
    assertThat(desc.getTags(), empty());
  }

  @Test
  public void descriptorSet() throws Exception {
    OperationStatisticDescriptor<StatisticType> desc = OperationStatisticDescriptor.descriptor("observer", asSet("a", "b"), StatisticType.class);
    assertDescriptor(desc);
  }

  @Test
  public void descriptorArray() throws Exception {
    OperationStatisticDescriptor<StatisticType> desc = OperationStatisticDescriptor.descriptor("observer", StatisticType.class, "a", "b");
    assertDescriptor(desc);
  }

  private void assertDescriptor(OperationStatisticDescriptor<StatisticType> desc) {
    assertThat(desc.getObserverName(), equalTo("observer"));
    assertThat(desc.getType(), Matchers.equalTo((Object) StatisticType.class));
    assertThat(desc.getTags(), containsInAnyOrder("a", "b"));
  }

  private Set<String> asSet(String... tags) {
    return new HashSet<>(Arrays.asList(tags));
  }

}
