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

import java.util.Set;

import org.terracotta.context.TreeNode;

class EnsureUnique implements Query {
  
  static Query INSTANCE = new EnsureUnique();
  
  private EnsureUnique() {
    //singleton
  }

  @Override
  public Set<TreeNode> execute(Set<TreeNode> input) {
    if (input.size() == 1) {
      return input;
    } else {
      throw new IllegalStateException("Expected a uniquely identified node: found " + input.size());
    }
  }
}
