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

  public static Matcher<? super TreeNode<?, ?, ?>> context(final Matcher<ContextElement<?, ?, ?>> matcher) {
    return new Matcher<TreeNode<?, ?, ?>>() {

      @Override
      public boolean matches(TreeNode<?, ?, ?> t) {
        return matcher.matches(t.getContext());
      }

      @Override
      public String toString() {
        return "a context that has " + matcher;
      }
    };
  }
  
  public static Matcher<ContextElement<?, ?, ?>> attributes(final Matcher<Map<?, ?>> matcher) {
    return new Matcher<ContextElement<?, ?, ?>>() {

      @Override
      public boolean matches(ContextElement<?, ?, ?> t) {
        return matcher.matches(t.attributes());
      }

      @Override
      public String toString() {
        return "an attributes " + matcher;
      }
    };
  }
  
  public static Matcher<ContextElement<?, ?, ?>> identifier(final Matcher<Object> matcher) {
    return new Matcher<ContextElement<?, ?, ?>>() {

      @Override
      public boolean matches(ContextElement<?, ?, ?> t) {
        return matcher.matches(t.identifier());
      }

      @Override
      public String toString() {
        return "an identifier that is " + matcher;
      }
    };
    
  }
  
  public static Matcher<Object> subclassOf(final Class klazz) {
    return new Matcher<Object>() {

      @Override
      public boolean matches(Object t) {
        return t instanceof Class && klazz.isAssignableFrom((Class) t);
      }

      @Override
      public String toString() {
        return "a subtype of " + klazz;
      }
    };
  }
  
  public static Matcher<Map<?, ?>> hasAttribute(final Object key, final Object value) {
    return new Matcher<Map<?, ?>>() {

      @Override
      public boolean matches(Map<?, ?> object) {
        return object.containsKey(key) && value.equals(object.get(key));
      }
    };
  }
  
  public static <T> Matcher<T> anyOf(final Matcher<? super T> ... matchers) {
    return new Matcher<T>() {

      @Override
      public boolean matches(T object) {
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
      public boolean matches(T object) {
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
