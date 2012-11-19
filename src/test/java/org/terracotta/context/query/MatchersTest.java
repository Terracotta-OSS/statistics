/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
