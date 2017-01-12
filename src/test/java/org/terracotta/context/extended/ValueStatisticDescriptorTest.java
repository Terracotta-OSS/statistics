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

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class ValueStatisticDescriptorTest {

  @Test
  public void getTags_empty() throws Exception {
    ValueStatisticDescriptor desc = ValueStatisticDescriptor.descriptor("observer");
    assertThat(desc.getTags(), empty());
  }

  @Test
  public void descriptorSet() throws Exception {
    ValueStatisticDescriptor desc = ValueStatisticDescriptor.descriptor("observer", asSet("a", "b"));
    assertDescriptor(desc);
  }

  @Test
  public void descriptorArray() throws Exception {
    ValueStatisticDescriptor desc = ValueStatisticDescriptor.descriptor("observer", "a", "b");
    assertDescriptor(desc);
  }

  private void assertDescriptor(ValueStatisticDescriptor desc) {
    assertThat(desc.getObserverName(), equalTo("observer"));
    assertThat(desc.getTags(), containsInAnyOrder("a", "b"));
  }

  private Set<String> asSet(String... tags) {
    return new HashSet<String>(Arrays.asList(tags));
  }
}
