/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context.query;

import java.util.Map;

import org.terracotta.context.ContextElement;
import org.terracotta.context.TreeNode;

public final class Matchers {
  
  private Matchers() {
    //static class
  }

  public static Matcher<TreeNode> context(final Matcher<ContextElement> matcher) {
    return new Matcher<TreeNode>() {

      @Override
      boolean matchesSafely(TreeNode t) {
        return matcher.matches(t.getContext());
      }

      @Override
      public String toString() {
        return "a context that has " + matcher;
      }
    };
  }
  
  public static Matcher<ContextElement> attributes(final Matcher<? extends Map<? extends String, ? extends Object>> matcher) {
    return new Matcher<ContextElement>() {

      @Override
      boolean matchesSafely(ContextElement t) {
        return matcher.matches(t.attributes());
      }

      @Override
      public String toString() {
        return "an attributes " + matcher;
      }
    };
  }
  
  public static Matcher<ContextElement> identifier(final Matcher<Class<?>> matcher) {
    return new Matcher<ContextElement>() {

      @Override
      boolean matchesSafely(ContextElement t) {
        return matcher.matches(t.identifier());
      }

      @Override
      public String toString() {
        return "an identifier that is " + matcher;
      }
    };
    
  }
  
  public static Matcher<Class<?>> subclassOf(final Class klazz) {
    return new Matcher<Class<?>>() {

      @Override
      boolean matchesSafely(Class<?> t) {
        return klazz.isAssignableFrom(t);
      }

      @Override
      public String toString() {
        return "a subtype of " + klazz;
      }
    };
  }
  
  public static <K, V> Matcher<Map<K, V>> hasAttribute(final K key, final V value) {
    return new Matcher<Map<K, V>>() {

      @Override
      boolean matchesSafely(Map<K, V> object) {
        return object.containsKey(key) && value.equals(object.get(key));
      }
    };
  }
  
  public static <K, V> Matcher<Map<K, V>> hasAttribute(final K key, final Matcher<V> value) {
    return new Matcher<Map<K, V>>() {

      @Override
      boolean matchesSafely(Map<K, V> object) {
        return object.containsKey(key) && value.matches(object.get(key));
      }
    };
  }
  
  public static <T> Matcher<T> anyOf(final Matcher<? super T> ... matchers) {
    return new Matcher<T>() {

      @Override
      boolean matchesSafely(T object) {
        for (Matcher<? super T> matcher : matchers) {
          if (matcher.matches(object)) {
            return true;
          }
        }
        return false;
      }
    };
  }

  public static <T> Matcher<T> allOf(final Matcher<? super T> ... matchers) {
    return new Matcher<T>() {

      @Override
      boolean matchesSafely(T object) {
        for (Matcher<? super T> matcher : matchers) {
          if (!matcher.matches(object)) {
            return false;
          }
        }
        return true;
      }
    };
  }
}
