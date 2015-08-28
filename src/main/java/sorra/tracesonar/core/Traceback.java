package sorra.tracesonar.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sorra.tracesonar.model.Method;

public class Traceback {

  public static void search(Method self) {
    if (self.owner.equals("*")) {
      GreatMap.INSTANCE.callerCollectors.keySet().forEach(Traceback::searchAndPrintTree);
      return;
    }
    if (self.methodName.equals("*")) {
      GreatMap.INSTANCE.callerCollectors.keySet().stream()
          .filter(callee -> callee.owner.equals(self.owner))
          .forEach(Traceback::searchAndPrintTree);
      return;
    }
    if (self.desc.equals("*")) {
      GreatMap.INSTANCE.callerCollectors.keySet().stream()
          .filter(callee -> callee.methodName.equals(self.methodName) && callee.owner.equals(self.owner))
          .forEach(Traceback::searchAndPrintTree);
    }
  }

  static void searchAndPrintTree(Method self) {
    printTree(searchTree(self, null), 0);
  }

  static TreeNode searchTree(Method self, TreeNode parent) {
    TreeNode cur = new TreeNode();
    cur.self = self;
    cur.parent = parent;
    CallerCollector callerCollector = GreatMap.INSTANCE.getCallerCollector(self);
    for (Method caller : callerCollector.getCallers()) {
      if (cur.findCycle(caller)) {
        TreeNode cycleEnd = new TreeNode();
        cycleEnd.self = caller;
        cur.callers.add(cycleEnd);
      } else {
        cur.callers.add(searchTree(caller, cur));
      }
    }
    return cur;
  }

  static void printTree(TreeNode node, int depth) {
    char[] indents = new char[depth];
    Arrays.fill(indents, '\t');
    System.out.println(String.valueOf(indents) + node.self);
    node.callers.forEach(c -> printTree(c, depth + 1));
  }

  static class TreeNode {
    Method self;
    TreeNode parent;
    List<TreeNode> callers = new ArrayList<>();

    boolean findCycle(Method neo) {
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
