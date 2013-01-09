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
package org.terracotta.context.query;

import java.util.Map;

import org.terracotta.context.ContextElement;
import org.terracotta.context.TreeNode;

/**
 * A static library of {@link Matcher} implementations that can be used with the
 * {@link QueryBuilder#filter(Matcher)} method.
 */
public final class Matchers {
  
  private Matchers() {
    //static class
  }

  /**
   * Returns a matcher that matches tree nodes whose {@link TreeNode#getContext()}
   * match against the supplied matcher.
   * 
   * @param matcher {@code ContextElement} matcher
   * @return a {@code TreeNode} matcher
   */
  public static Matcher<TreeNode> context(final Matcher<ContextElement> matcher) {
    return new Matcher<TreeNode>() {

      @Override
      protected boolean matchesSafely(TreeNode t) {
        return matcher.matches(t.getContext());
      }

      @Override
      public String toString() {
        return "a context that has " + matcher;
      }
    };
  }
  
  /**
   * Returns a matcher that matches context elements whose {@link ContextElement#attributes()}
   * match against the supplied matcher.
   * 
   * @param matcher a {@code Map} (attributes) matcher
   * @return a {@code ContextElement} matcher
   */
  public static Matcher<ContextElement> attributes(final Matcher<Map<String, Object>> matcher) {
    return new Matcher<ContextElement>() {

      @Override
      protected boolean matchesSafely(ContextElement t) {
        return matcher.matches(t.attributes());
      }

      @Override
      public String toString() {
        return "an attributes " + matcher;
      }
    };
  }
  
  /**
   * Returns a matcher that matches context elements whose {@link ContextElement#identifier()}
   * match against the supplied matcher.
   * 
   * @param matcher {@code Class<?>} matcher
   * @return a {@code ContextElement} matcher
   */
  public static Matcher<ContextElement> identifier(final Matcher<Class<?>> matcher) {
    return new Matcher<ContextElement>() {

      @Override
      protected boolean matchesSafely(ContextElement t) {
        return matcher.matches(t.identifier());
      }

      @Override
      public String toString() {
        return "an identifier that is " + matcher;
      }
    };
    
  }
  
  /**
   * Returns a matcher that matches classes that are sub-types of the  supplied
   * class.
   * 
   * @param klazz a potential super-type
   * @return a {@code Class<?>} matcher
   */
  public static Matcher<Class<?>> subclassOf(final Class<?> klazz) {
    return new Matcher<Class<?>>() {

      @Override
      protected boolean matchesSafely(Class<?> t) {
        return klazz.isAssignableFrom(t);
      }

      @Override
      public String toString() {
        return "a subtype of " + klazz;
      }
    };
  }
  
  /**
   * Returns a matcher that matches attribute maps that include the given
   * attribute entry.
   * 
   * @param key attribute name
   * @param value attribute value
   * @return a {@code Map<String, Object>} matcher
   */
  public static Matcher<Map<String, Object>> hasAttribute(final String key, final Object value) {
    return new Matcher<Map<String, Object>>() {

      @Override
      protected boolean matchesSafely(Map<String, Object> object) {
        return object.containsKey(key) && value.equals(object.get(key));
      }
    };
  }
  
  /**
   * Returns a matcher that matches attribute maps the include an attribute with
   * the given name, and whose value matches the given matcher.
   * 
   * @param key attribute name
   * @param value attribute value matcher
   * @return a {@code Map<String, Object>} matcher
   */
  public static Matcher<Map<String, Object>> hasAttribute(final String key, final Matcher<? extends Object> value) {
    return new Matcher<Map<String, Object>>() {

      @Override
      protected boolean matchesSafely(Map<String, Object> object) {
        return object.containsKey(key) && value.matches(object.get(key));
      }
    };
  }
  
  /**
   * Returns a matcher that matches when against objects which match <em>any</em>
   * of the supplied matchers.
   * 
   * @param <T> type of the object to be matched
   * @param matchers list of matchers to match
   * @return a compound matcher
   */
  public static <T> Matcher<T> anyOf(final Matcher<? super T> ... matchers) {
    return new Matcher<T>() {

      @Override
      protected boolean matchesSafely(T object) {
        for (Matcher<? super T> matcher : matchers) {
          if (matcher.matches(object)) {
            return true;
          }
        }
        return false;
      }
    };
  }

  /**
   * Returns a matcher that matches when against objects which match <em>all</em>
   * of the supplied matchers.
   * 
   * @param <T> type of the object to be matched
   * @param matchers list of matchers to match
   * @return a compound matcher
   */
  public static <T> Matcher<T> allOf(final Matcher<? super T> ... matchers) {
    return new Matcher<T>() {

      @Override
      protected boolean matchesSafely(T object) {
        for (Matcher<? super T> matcher : matchers) {
          if (!matcher.matches(object)) {
            return false;
          }
        }
        return true;
      }
    };
  }
  
  public static <T> Matcher<T> not(final Matcher<T> matcher) {
    return new Matcher<T>() {

      @Override
      protected boolean matchesSafely(T object) {
        return !matcher.matches(object);
      }
    };
  }
}
