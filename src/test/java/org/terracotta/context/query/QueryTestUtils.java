/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
  
  public static <I, K, V> TreeNode<I, K, V> createTreeNode(I identifier) {
    return createTreeNode(identifier, Collections.<K, V>emptyMap());
  }
  
  public static <I, K, V> TreeNode<I, K, V> createTreeNode(I identifier, Map<K, V> attributes) {
    return createTreeNode(identifier, attributes, Collections.<TreeNode<I, K, V>>emptySet());
  }
  
  public static <I, K, V> TreeNode<I, K, V> createTreeNode(I identifier, Set<TreeNode<I, K, V>> children) {
    return createTreeNode(identifier, Collections.<K, V>emptyMap(), children);
  }
  
  public static <I, K, V> TreeNode<I, K, V> createTreeNode(I identifier, Map<K, V> attributes, Set<TreeNode<I, K, V>> children) {
    return new ImmutableTreeNode<I, K, V>(children, new ImmutableContextElement<I, K, V>(identifier, attributes));
  }
  
  static class ImmutableTreeNode<I, K, V> implements TreeNode<I, K, V> {
    
    private final Set<? extends TreeNode<I, K, V>> children;
    private final ContextElement<I, K, V> context;

    public ImmutableTreeNode(Set<? extends TreeNode<I, K, V>> children, ContextElement<I, K, V> context) {
      this.children = Collections.unmodifiableSet(children);
      this.context = context;
    }

    @Override
    public Set<? extends TreeNode<I, K, V>> getChildren() {
      return children;
    }

    @Override
    public List<? extends TreeNode<I, K, V>> getPath() {
      throw new IllegalStateException();
    }

    @Override
    public Collection<List<? extends TreeNode<I, K, V>>> getPaths() {
      return Collections.emptyList();
    }

    @Override
    public ContextElement<I, K, V> getContext() {
      return context;
    }
    
    @Override
    public String toString() {
      return context.toString();
    }
  }
  
  static class ImmutableContextElement<I, K, V> implements ContextElement<I, K, V> {

    private final I identifier;
    private final Map<K, V> attributes;

    public ImmutableContextElement(I identifier, Map<K, V> attributes) {
      this.identifier = identifier;
      this.attributes = Collections.unmodifiableMap(attributes);
    }

    @Override
    public I identifier() {
      return identifier;
    }

    @Override
    public Map<K, V> attributes() {
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
}
