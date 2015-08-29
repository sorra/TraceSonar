package sorra.tracesonar.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

import sorra.tracesonar.model.Method;

public class Traceback {
  private StringBuilder output = new StringBuilder();
  private BiConsumer<TreeNode, Integer> printer = (node, depth) -> {
    char[] indents = new char[depth];
    Arrays.fill(indents, '\t');
    output.append(String.valueOf(indents) + node.self).append('\n');
  };

  public static CharSequence search(Method self, boolean html) {
    Traceback traceback = new Traceback();
    if (html) {
      traceback.printer = (node, depth) -> {
        if (depth == 0) {
          traceback.output.append(String.format(
              "<div class=\"queried\">%s</div>\n", node.self));
        } else {
          traceback.output.append(String.format(
              "<div class=\"%s\" style=\"margin-left:%dem\">%s</div>\n",
              node.callers.isEmpty() ? "caller endpoint" : "caller", depth*5, node.self));
        }
      };
    }
    if (self.owner.equals("*")) {
      GreatMap.INSTANCE.callerCollectors.keySet().forEach(traceback::searchAndPrintTree);
      return traceback.output;
    }
    if (self.methodName.equals("*")) {
      GreatMap.INSTANCE.callerCollectors.keySet().stream()
          .filter(callee -> callee.owner.equals(self.owner))
          .forEach(traceback::searchAndPrintTree);
      return traceback.output;
    }
    if (self.desc.equals("*")) {
      GreatMap.INSTANCE.callerCollectors.keySet().stream()
          .filter(callee -> callee.methodName.equals(self.methodName) && callee.owner.equals(self.owner))
          .forEach(traceback::searchAndPrintTree);
    }
    return traceback.output;
  }

  void searchAndPrintTree(Method self) {
    printTree(searchTree(self, null), 0);
  }

  TreeNode searchTree(Method self, TreeNode parent) {
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

  void printTree(TreeNode node, int depth) {
    printer.accept(node, depth);
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
