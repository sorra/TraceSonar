package sorra.tracesonar.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sorra.tracesonar.model.Caller;
import sorra.tracesonar.util.Strings;

public class Traceback {
  public static TreeNode search(Caller self, TreeNode parent) {
    TreeNode cur = new TreeNode();
    cur.self = self;
    cur.parent = parent;
    CallerCollector callerCollector = GreatMap.INSTANCE.getCallerCollector(
        Strings.substringBefore(self.owner, "$"));
    for (Caller caller : callerCollector.getCallers()) {
      if (cur.findCycle(caller)) {
        TreeNode cycleEnd = new TreeNode();
        cycleEnd.self = caller;
        cur.callers.add(cycleEnd);
      } else {
        cur.callers.add(search(caller, cur));
      }
    }
    return cur;
  }

  public static void printTree(TreeNode node, int depth) {
    char[] indents = new char[depth];
    Arrays.fill(indents, '\t');
    System.out.println(String.valueOf(indents) + node.self);
    node.callers.forEach(c -> printTree(c, depth + 1));
  }

  static class TreeNode {
    Caller self;
    TreeNode parent;
    List<TreeNode> callers = new ArrayList<>();

    boolean findCycle(Caller neo) {
      TreeNode cur = this;
      while (cur != null) {
        if (neo.equals(cur.self)) {
          return true;
        }
        cur = cur.parent;
      }
      return false;
    }
  }
}
