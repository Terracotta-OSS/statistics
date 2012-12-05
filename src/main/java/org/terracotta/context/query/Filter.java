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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.terracotta.context.TreeNode;

class Filter implements Query {

  private final Matcher<? super TreeNode> filter;
  
  public Filter(Matcher<? super TreeNode> filter) {
    if (filter == null) {
      throw new NullPointerException("Cannot filter using a null matcher");
    } else {
      this.filter = filter;
    }
  }
  
  @Override
  public Set<TreeNode> execute(Set<TreeNode> input) {
    Set<TreeNode> output = new HashSet<TreeNode>(input);
    for (Iterator<TreeNode> it = output.iterator(); it.hasNext(); ) {
      if (!filter.matches(it.next())) {
        it.remove();
      }
    }
    return output;
  }

  @Override
  public String toString() {
    return "filter for nodes with " + filter;
  }
}
