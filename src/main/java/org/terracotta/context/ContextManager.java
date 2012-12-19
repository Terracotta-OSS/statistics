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
package org.terracotta.context;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terracotta.context.annotations.ContextChild;
import org.terracotta.context.annotations.ContextParent;
import org.terracotta.context.extractor.ObjectContextExtractor;
import org.terracotta.context.query.Query;
import org.terracotta.context.query.QueryBuilder;

import static org.terracotta.context.query.QueryBuilder.*;

/**
 * A {@code ContextManager} instances allows for rooting, querying and access
 * to select portions of the global context graph.
 */
public class ContextManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(ContextManager.class);
  private static final WeakIdentityHashMap<Object, MutableTreeNode> CONTEXT_OBJECTS = new WeakIdentityHashMap<Object, MutableTreeNode>();
  private static final Collection<ContextCreationListener> contextCreationListeners = new CopyOnWriteArrayList<ContextCreationListener>();
  
  private final RootNode root = new RootNode();
  
  /**
   * Create an {@code Association} instance for the supplied object.
   * 
   * @param object the object to be associated
   * @return an association instance
   */
  public static Association associate(final Object object) {
    return new Association() {

      @Override
      public Association withChild(Object child) {
        associate(child, object);
        return this;
      }

      @Override
      public Association withParent(Object parent) {
        associate(object, parent);
        return this;
      }
    };
  }
  
  /**
   * Create a {@code Dissociation} instance for the supplied object.
   * 
   * @param object the object to be dissociated
   * @return a dissociation instance
   */
  public static Dissociation dissociate(final Object object) {
    return new Dissociation() {

      @Override
      public Dissociation fromChild(Object child) {
        dissociate(child, object);
        return this;
      }

      @Override
      public Dissociation fromParent(Object parent) {
        dissociate(object, parent);
        return this;
      }
    };
  }
  
  /**
   * Return the {@code TreeNode} associated with this object.
   * <p>
   * Returns {@code null} if the supplied object has no associated context node.
   * 
   * @param object object to lookup node for
   * @return {@code TreeNode} associated with this object
   */
  public static TreeNode nodeFor(Object object) {
    return getTreeNode(object);
  }

  public static void registerContextCreationListener(ContextCreationListener listener) {
    contextCreationListeners.add(listener);
  }
  
  public static void deregisterContextCreationListener(ContextCreationListener listener) {
    contextCreationListeners.remove(listener);
  }
  
  private static void associate(Object child, Object parent) {
    getOrCreateTreeNode(parent).addChild(getOrCreateTreeNode(child));
  }
  
  private static void dissociate(Object child, Object parent) {
    getTreeNode(parent).removeChild(getTreeNode(child));
  }
  
  private static MutableTreeNode getTreeNode(Object object) {
    return CONTEXT_OBJECTS.get(object);
  }
  
  private static MutableTreeNode getOrCreateTreeNode(Object object) {
    MutableTreeNode node = CONTEXT_OBJECTS.get(object);
    
    if (node == null) {
      ContextElement context = ObjectContextExtractor.extract(object);
      node = new MutableTreeNode(context);
      MutableTreeNode racer = CONTEXT_OBJECTS.putIfAbsent(object, node);
      if (racer != null) {
        return racer;
      } else {
        discoverAssociations(object);
        contextCreated(object);
        return node;
      }
    } else {
      return node;
    }
  }

  private static void discoverAssociations(Object origin) {
    for (Class c = origin.getClass(); c != null; c = c.getSuperclass()) {
      for (Field f : c.getDeclaredFields()) {
        if (f.isAnnotationPresent(ContextChild.class)) {
          f.setAccessible(true);
          Object child;
          try {
            child = f.get(origin);
          } catch (IllegalArgumentException ex) {
            throw new AssertionError(ex);
          } catch (IllegalAccessException ex) {
            LOGGER.warn("Failed to traverse {} due to: {}", f, ex);
            continue;
          }
          if (child != null) {
            associate(child, origin);
          }
        }
        if (f.isAnnotationPresent(ContextParent.class)) {
          f.setAccessible(true);
          Object parent;
          try {
            parent = f.get(origin);
          } catch (IllegalArgumentException ex) {
            throw new AssertionError(ex);
          } catch (IllegalAccessException ex) {
            LOGGER.warn("Failed to traverse {} due to: {}", f, ex);
            continue;
          }
          if (parent != null) {
            associate(origin, parent);
          }
        }
      }
    }
  }
  
  private static void contextCreated(Object object) {
    for (ContextCreationListener listener : contextCreationListeners) {
      listener.contextCreated(object);
    }
  }

  /**
   * Root the given object's context node in this {@code ContextManager}
   * instance.
   * 
   * @param object object whose context will be rooted
   */
  public void root(Object object) {
    root.addChild(getOrCreateTreeNode(object));
  }
  
  /**
   * Remove the given object's context node from this {@code ContextManager} 
   * root set.
   * 
   * @param object object whose context will be uprooted
   */
  public void uproot(Object object) {
    root.removeChild(getTreeNode(object));
  }
  
  /**
   * Run the supplied {@code Query} against this {@code ContextManager}'s 
   * root context.
   * <p>
   * The initial node in the queries traversal will be the node whose children
   * form the root set of this {@code ContextManager}.  That is, the following
   * code will select the root set of this instance.<br>
   * <pre>
   * public static Set<TreeNode> roots(ContextManager manager) {
   *   return manager.query(QueryBuilder.queryBuilder().children().build());
   * }
   * </pre>
   * 
   * @param query the query to execute
   * @return the set of nodes selected by the query
   */
  public Set<TreeNode> query(Query query) {
    return query.execute(Collections.<TreeNode>singleton(root));
  }

  /**
   * Return the unique node selected by running this query against this 
   * {@code ContextManager}'s root context.
   * <p>
   * If this query does not return a single unique result then an
   * {@code IllegalStateException} will be thrown.  More details on the query 
   * execution context can be found in {@link #query(Query)}.
   * 
   * @see #query(Query)
   * @see QueryBuilder#ensureUnique()
   * @param query the query to execute
   * @return the node selected by the query
   * @throws IllegalStateException if the query does not select a unique node
   */
  public TreeNode queryForSingleton(Query query) throws IllegalStateException {
    return query(queryBuilder().chain(query).ensureUnique().build()).iterator().next();
  }
  
  /**
   * Registers a listener for additions and removals to this 
   * {@code ContextManager}'s context graph.
   * 
   * @param listener listener to be registered
   */
  public void registerContextListener(ContextListener listener) {
    root.addListener(listener);
  }
  
  /**
   * Removes a previously registered listener from the listener set.
   * 
   * @param listener listener to be deregistered
   */
  public void deregisterContextListener(ContextListener listener) {
    root.removeListener(listener);
  }
  
  /**
   * Creates parent and child associations to the target context node.
   * <p>
   * Mutations performed to the parent and child node sets of the target node
   * are also accompanied by the equivalent changes to the reverse relationship
   * in the supplied object's context node.  This ensures that parent/child
   * relationships are properly consistent.
   */
  public interface Association {
    
    /**
     * Adds the supplied object's context node as a child of the target context
     * node.
     * 
     * @param child object whose context node will be associated
     * @return this association object
     */
    Association withChild(Object child);
    
    /**
     * Adds the supplied object's context node as a parent of the target context
     * node.
     * 
     * @param parent object whose context node will be associated
     * @return this association object
     */
    Association withParent(Object parent);
  }
  
  /**
   * Removes existing parent and child associations from the target context node.
   * <p>
   * Mutations performed to the parent and child node sets of the target node
   * are also accompanied by the equivalent changes to the reverse relationship
   * in the supplied object's context node.  This ensures that parent/child
   * relationships are properly consistent.
   */
  public interface Dissociation {
    
    /**
     * Removes the supplied object's context from the child node set of the
     * target context node.
     * 
     * @param child object whose context node will be dissociated
     * @return this dissociation object
     */
    Dissociation fromChild(Object child);
    
    /**
     * Removes the supplied object's context from the parent node set of the
     * target context node.
     * 
     * @param parent object whose context node will be dissociated
     * @return this dissociation object
     */
    Dissociation fromParent(Object parent);
  }
}
