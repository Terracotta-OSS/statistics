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
package org.terracotta.statistics;

import java.lang.reflect.Field;
import java.util.Stack;

/**
 * @author cdennis
 */
public class TimeMocking {

  public static final Stack<Time.TimeSource> SOURCES = new Stack<>();
  private static final Field TIME_SOURCE_FIELD;

  static {
    try {
      TIME_SOURCE_FIELD = Time.class.getDeclaredField("TIME_SOURCE");
      TIME_SOURCE_FIELD.setAccessible(true);
    } catch (NoSuchFieldException | SecurityException ex) {
      throw new AssertionError(ex);
    }
  }

  public synchronized static <T extends Time.TimeSource> T push(T source) {
    try {
      SOURCES.push((Time.TimeSource) TIME_SOURCE_FIELD.get(null));
      TIME_SOURCE_FIELD.set(null, source);
    } catch (IllegalArgumentException | IllegalAccessException ex) {
      throw new AssertionError(ex);
    }
    return source;
  }

  public synchronized static Time.TimeSource pop() {
    try {
      Time.TimeSource popped = (Time.TimeSource) TIME_SOURCE_FIELD.get(null);
      TIME_SOURCE_FIELD.set(null, SOURCES.pop());
      return popped;
    } catch (IllegalArgumentException | IllegalAccessException ex) {
      throw new AssertionError(ex);
    }
  }
}
