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

import java.util.Collection;

import org.hamcrest.collection.IsEmptyCollection;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Test;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

/**
 *
 * @author cdennis
 */
public class MutableTreeNodeTest {
 
  @Test
  public void testCleanDisconnects() {
    MutableTreeNode test = new MutableTreeNode(null);
    
    MutableTreeNode child = new MutableTreeNode(null);
    MutableTreeNode parent = new MutableTreeNode(null);
    
    test.addChild(child);
    parent.addChild(test);
    parent.addChild(child);
    
    test.clean();
    
    assertThat((Collection<AbstractTreeNode>) test.getChildren(), IsEmptyCollection.<AbstractTreeNode>empty());
    assertThat((Collection<AbstractTreeNode>) test.getAncestors(), IsEmptyCollection.<AbstractTreeNode>empty());
    
    assertThat(parent.getChildren(), hasSize(1));
    assertThat((Collection<AbstractTreeNode>) parent.getChildren(), IsIterableContainingInOrder.<AbstractTreeNode>contains(child));
    
    assertThat(child.getAncestors(), hasSize(1));
    assertThat((Collection<AbstractTreeNode>) child.getAncestors(), IsIterableContainingInOrder.<AbstractTreeNode>contains(parent));
  }
}
