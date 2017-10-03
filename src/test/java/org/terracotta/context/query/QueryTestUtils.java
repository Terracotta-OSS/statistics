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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.terracotta.context.ContextElement;
import org.terracotta.context.TreeNode;

/**
 *
 * @author cdennis
 */
public class QueryTestUtils {
  
  public static  TreeNode createTreeNode(Class identifier) {
    return createTreeNode(identifier, Collections.emptyMap());
  }
  
  public static  TreeNode createTreeNode(Class identifier, Map<String, Object> attributes) {
    return createTreeNode(identifier, attributes, Collections.emptySet());
  }
  
  public static  TreeNode createTreeNode(Class identifier, Set<TreeNode> children) {
    return createTreeNode(identifier, Collections.emptyMap(), children);
  }
  
  public static  TreeNode createTreeNode(Class identifier, Map<String, Object> attributes, Set<TreeNode> children) {
    return new ImmutableTreeNode(children, new ImmutableContextElement(identifier, attributes));
  }
  
  static class ImmutableTreeNode implements TreeNode {
    
    private final Set<? extends TreeNode> children;
    private final ContextElement context;

    public ImmutableTreeNode(Set<? extends TreeNode> children, ContextElement context) {
      this.children = Collections.unmodifiableSet(children);
      this.context = context;
    }

    @Override
    public Set<? extends TreeNode> getChildren() {
      return children;
    }

    @Override
    public List<? extends TreeNode> getPath() {
      throw new IllegalStateException();
    }

    @Override
    public Collection<List<? extends TreeNode>> getPaths() {
      return Collections.emptyList();
    }

    @Override
    public ContextElement getContext() {
      return context;
    }
    
    @Override
    public String toString() {
      return context.toString();
    }

    @Override
    public String toTreeString() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void clean() {
      throw new UnsupportedOperationException("Not supported yet.");
    }
  }
  
  static class ImmutableContextElement implements ContextElement {

    private final Class identifier;
    private final Map<String, Object> attributes;

    public ImmutableContextElement(Class identifier, Map<String, Object> attributes) {
      this.identifier = identifier;
      this.attributes = Collections.unmodifiableMap(attributes);
    }

    @Override
    public Class identifier() {
      return identifier;
    }

    @Override
    public Map<String, Object> attributes() {
      return attributes;
    }
    
    @Override
    public String toString() {
      if (attributes.isEmpty()) {
        return "Node:" + identifier;
      } else {
        return "Node:" + identifier + " " + attributes.toString();
      }
    }
  }
  
  public static class A {}
  public static class B {}
  public static class C {}
  public static class D {}
  public static class E {}
  public static class F {}
  public static class G {}
  public static class H {}
  public static class I {}
  public static class J {}
}
