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
package org.terracotta.statistics.archive;

import org.junit.Test;

import static org.hamcrest.collection.IsArrayContainingInOrder.*;
import static org.hamcrest.collection.IsArrayWithSize.*;
import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsInstanceOf.*;
import static org.hamcrest.core.IsNull.*;
import static org.junit.Assert.assertThat;

/**
 *
 * @author cdennis
 */
public class CircularBufferTest {
  
  @Test
  public void testEmptyBuffer() {
    CircularBuffer<Object> buffer = new CircularBuffer<Object>(2);
    assertThat(buffer.toArray(Object[].class), emptyArray());
  }
  
  @Test
  public void testSingleElementInBuffer() {
    CircularBuffer<Object> buffer = new CircularBuffer<Object>(2);
    Object object = new Object();
    assertThat(buffer.insert(object), nullValue());
    assertThat(buffer.toArray(Object[].class), arrayContaining(object));
  }

  @Test
  public void testFullBuffer() {
    CircularBuffer<Object> buffer = new CircularBuffer<Object>(2);
    Object object1 = new Object();
    Object object2 = new Object();
    assertThat(buffer.insert(object1), nullValue());
    assertThat(buffer.insert(object2), nullValue());
    assertThat(buffer.toArray(Object[].class), arrayContaining(object1, object2));
  }

  @Test
  public void testBufferOverspill() {
    CircularBuffer<Object> buffer = new CircularBuffer<Object>(2);
    Object object1 = new Object();
    Object object2 = new Object();
    Object object3 = new Object();
    assertThat(buffer.insert(object1), nullValue());
    assertThat(buffer.insert(object2), nullValue());
    assertThat(buffer.insert(object3), is(object1));
    assertThat(buffer.toArray(Object[].class), arrayContaining(object2, object3));
  }
  
  @Test
  public void testGenericBuffer() {
    CircularBuffer<String> buffer = new CircularBuffer<String>(2);
    String string1 = "foo";
    String string2 = "bar";
    String string3 = "baz";
    assertThat(buffer.insert(string1), nullValue());
    assertThat(buffer.insert(string2), nullValue());
    assertThat(buffer.insert(string3), is(string1));
    assertThat(buffer.toArray(String[].class), instanceOf(String[].class));
    assertThat(buffer.toArray(String[].class), arrayContaining(string2, string3));
    assertThat(buffer.toArray(Object[].class), instanceOf(Object[].class));
    assertThat(buffer.toArray(Object[].class), arrayContaining((Object) string2, (Object) string3));
  }
}
