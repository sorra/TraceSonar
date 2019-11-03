package sorra.tracesonar.core;

import java.util.ArrayList;
import java.util.List;

import sorra.tracesonar.model.Method;

public class TreeNode {
  Method self;
  boolean isCallingSuper; // self is calling the super method of parent
  TreeNode parent;
  int depth;
  List<TreeNode> callers = new ArrayList<>();

  TreeNode(Method self, boolean isCallingSuper, TreeNode parent) {
    this.self = self;
    this.isCallingSuper = isCallingSuper;
    this.parent = parent;

    if (parent == null) {
      depth = 0;
    } else {
      depth = parent.depth + 1;
    }
  }

  boolean findCycle(Method neo) {
    TreeNode cur = this;
    do {
      if (cur.self.equals(neo) || cur.callers.stream().anyMatch(x -> x.self.equals(neo))) {
        return true;
      }
      cur = cur.parent;
    } while (cur != null);

    return false;
  }

  void addCycleEnd(Method caller, boolean asSuper) {
    TreeNode cycleEnd = new TreeNode(caller, asSuper, this);
    callers.add(cycleEnd);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    TreeNode cur = this;
    do {
      sb.append(cur.depth);
      for (int i = 0; i < cur.depth; i++) {
        sb.append("  ");
      }
      sb.append(cur.self).append('\n');
      cur = cur.parent;
    } while (cur != null);

    return sb.toString();
  }
}
