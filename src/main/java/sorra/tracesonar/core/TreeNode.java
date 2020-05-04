package sorra.tracesonar.core;

import java.util.ArrayList;
import java.util.List;

import sorra.tracesonar.model.Method;

public class TreeNode {
  final Method self;
  final boolean isCallingSuper; // self is calling the super method of parent
  final TreeNode parent;
  final int depth;

  final List<TreeNode> callers = new ArrayList<>();

  private String error;

  TreeNode(Method self, boolean isCallingSuper, TreeNode parent) {
    this.self = self;
    this.isCallingSuper = isCallingSuper;
    this.parent = parent;

    if (parent == null) {
      depth = 0;
    } else {
      depth = parent.depth + 1;
      parent.callers.add(this);
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

  boolean hasError() {
    return error != null;
  }

  String getError() {
    return error;
  }

  void setError(String error) {
    this.error = error;
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
