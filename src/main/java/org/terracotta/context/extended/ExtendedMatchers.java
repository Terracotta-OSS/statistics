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

import org.terracotta.context.query.Matcher;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static org.terracotta.context.query.Matchers.hasAttribute;

/**
 * Matchers specific to standard extended properties
 */
public final class ExtendedMatchers {

  private ExtendedMatchers() {
  }

  /**
   * Returns a matcher that checks if the "tags" attribute contains the tag.
   *
   * @param tag tag searched
   * @return a {@code Map<String, Object>} matcher for the tag
   */
  public static Matcher<Map<String, Object>> hasTag(final String tag) {
    return hasAttribute("tags", new Matcher<Set<String>>() {
      @Override
      protected boolean matchesSafely(Set<String> object) {
        return object.contains(tag);
      }
    });
  }

  /**
   * Returns a matcher that checks if the "tags" attribute contains all the tags.
   *
   * @param tags tags searched
   * @return a {@code Map<String, Object>} matcher for the tags
   */
  public static Matcher<Map<String, Object>> hasTags(final String... tags) {
    return hasTags(Arrays.asList(tags));
  }

  /**
   * Returns a matcher that checks if the "tags" attribute contains all the tags.
   *
   * @param tags tags searched
   * @return a {@code Map<String, Object>} matcher for the tags
   */
  public static Matcher<Map<String, Object>> hasTags(final Collection<String> tags) {
    return hasAttribute("tags", new Matcher<Set<String>>() {
      @Override
      protected boolean matchesSafely(Set<String> object) {
        return object.containsAll(tags);
      }
    });
  }

  /**
   * Returns a matcher that checks if the "properties" attribute contains an entry for {@code key}.
   *
   * @param key key searched
   * @return a {@code Map<String, Object>} matcher for the key
   */
  public static Matcher<Map<String, Object>> hasProperty(final String key) {
    return hasAttribute("properties", new Matcher<Map<String, Object>>() {
      @Override
      protected boolean matchesSafely(Map<String, Object> properties) {
        return properties.containsKey(key);
      }
    });
  }

  /**
   * Returns a matcher that checks if the "properties" attribute contains an entry for {@code key} and a value equals
   * to value.
   *
   * @param key key searched
   * @param value value searched
   * @return a {@code Map<String, Object>} matcher for the key and value
   */
  public static Matcher<Map<String, Object>> hasProperty(final String key, final String value) {
    return hasAttribute("properties", new Matcher<Map<String, Object>>() {
      @Override
      protected boolean matchesSafely(Map<String, Object> properties) {
        Object val = properties.get(key);
        return val == null ? false : value.equals(val);
      }
    });
  }
}
