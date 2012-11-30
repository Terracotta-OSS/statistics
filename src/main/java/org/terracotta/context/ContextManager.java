/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;

import org.terracotta.context.annotations.ContextChild;
import org.terracotta.context.annotations.ContextParent;
import org.terracotta.context.extractor.ObjectContextExtractor;
import org.terracotta.context.query.Query;
import org.terracotta.context.util.WeakIdentityHashMap;

import static org.terracotta.context.query.QueryBuilder.*;

public class ContextManager {

  private static final WeakIdentityHashMap<Object, MutableTreeNode> contextObjects = new WeakIdentityHashMap<Object, MutableTreeNode>();

  private final RootNode root = new RootNode();
  
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
  
  public static TreeNode nodeFor(Object object) {
    return getTreeNode(object);
  }
  
  private static void associate(Object child, Object parent) {
    getOrCreateTreeNode(parent).addChild(getOrCreateTreeNode(child));
  }
  
  private static void dissociate(Object child, Object parent) {
    getTreeNode(parent).removeChild(getTreeNode(child));
  }
  
  private static MutableTreeNode getTreeNode(Object object) {
    return contextObjects.get(object);
  }
  
  private static MutableTreeNode getOrCreateTreeNode(Object object) {
    MutableTreeNode node = contextObjects.get(object);
    
    if (node == null) {
      ContextElement context = ObjectContextExtractor.extract(object);
      node = new MutableTreeNode(context);
      MutableTreeNode racer = contextObjects.putIfAbsent(object, node);
      if (racer != null) {
        return racer;
      } else {
        discoverAssociations(object);
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
            //XXX we should log this failure to traverse
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
            //XXX we should log this failure to traverse
            continue;
          }
          if (parent != null) {
            associate(origin, parent);
          }
        }
      }
    }
  }

  public void root(Object object) {
    root.addChild(getOrCreateTreeNode(object));
  }
  
  public void uproot(Object object) {
    root.removeChild(getTreeNode(object));
  }
  
  public Collection<TreeNode> query(Query query) {
    return query.execute(Collections.<TreeNode>singleton(root));
  }

  public TreeNode queryForSingleton(Query query) {
    return query(queryBuilder().chain(query).ensureUnique().build()).iterator().next();
  }
  
  public void registerContextListener(ContextListener listener) {
    root.addListener(listener);
  }
  
  public void deregisterContextListener(ContextListener listener) {
    root.removeListener(listener);
  }
  
  public interface Association {
    
    Association withChild(Object child);
    
    Association withParent(Object parent);
  }
  
  public interface Dissociation {
    
    Dissociation fromChild(Object child);
    
    Dissociation fromParent(Object parent);
  }
}
