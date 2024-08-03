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
package org.terracotta.context.query;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author cdennis
 */
public class MatchersTest {
  
  @Test
  public void testSubclassOfSameClass() {
    assertTrue(Matchers.subclassOf(Integer.class).matches(Integer.class));
  }

  @Test
  public void testSubclassOfSuperClass() {
    assertTrue(Matchers.subclassOf(Number.class).matches(Integer.class));
  }

  @Test
  public void testSubclassOfInterface() {
    assertTrue(Matchers.subclassOf(Comparable.class).matches(Integer.class));
  }

  @Test
  public void testSubclassOfUnrelatedInterface() {
    assertFalse(Matchers.subclassOf(CharSequence.class).matches(Integer.class));
  }

  @Test
  public void testSubclassOfUnrelatedClass() {
    assertFalse(Matchers.subclassOf(Runtime.class).matches(Integer.class));
  }
  
  @Test
  public void testHasAttributeOnEmptyMap() {
    assertFalse(Matchers.hasAttribute("foo", "bar").matches(Collections.emptyMap()));
  }
  
  public void testHasAttributeOnSingletonMatchingMap() {
    assertTrue(Matchers.hasAttribute("foo", "bar").matches(Collections.singletonMap("foo", "bar")));
  }
  
  public void testHasAttributeOnSingletonNonMatchingMap() {
    assertFalse(Matchers.hasAttribute("foo", "bar").matches(Collections.singletonMap("foo", "baz")));
  }
  
  public void testHasAttributeOnMatchingMap() {
    Map<String, String> map = new HashMap<String, String>();
    map.put("foo", "bar");
    map.put("alice", "bob");
    assertTrue(Matchers.hasAttribute("foo", "bar").matches(map));
  }

  public void testHasAttributeOnNonMatchingMap() {
    Map<String, String> map = new HashMap<String, String>();
    map.put("foo", "baz");
    map.put("alice", "bob");
    assertTrue(Matchers.hasAttribute("foo", "bar").matches(map));
  }
}
