package sorra.tracesonar.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sorra.tracesonar.model.Method;

public class Traceback {
  private StringBuilder output = new StringBuilder();
  private Printer printer;

  public static CharSequence search(Method self, boolean html) {
    Traceback traceback = new Traceback();
    if (html) {
      traceback.output.append("<h3>").append(self).append("</h3>\n");
      traceback.printer = (node, depth) -> {
        if (depth == 0) {
          traceback.output.append(String.format(
              "<div class=\"queried\">%s</div>\n", node.self));
        } else {
          String cssClass = "caller";
          if (node.callers.isEmpty()) cssClass += " endpoint";
          if (node.isCallingSuper) cssClass += " potential";

          traceback.output.append(String.format(
              "<div class=\"%s\" style=\"margin-left:%dem\">%s</div>\n", cssClass, depth*5, node.self));
        }
      };
    } else {
      traceback.output.append(self).append("\n");
      traceback.printer = (node, depth) -> {
        char[] indents = new char[depth];
        Arrays.fill(indents, '\t');
        traceback.output.append(String.valueOf(indents)).append(node.self).append('\n');
      };
    }
    
    if (self.owner.equals("*")) {
      ClassMap.INSTANCE.classOutlines.values().forEach(co -> {
        co.methods.forEach(traceback::searchAndPrintTree);
      });
      return traceback.output;
    }
    if (self.methodName.equals("*")) {
      ClassMap.INSTANCE.classOutlines.get(self.owner).methods.stream()
          .filter(x -> x.owner.equals(self.owner))
          .forEach(traceback::searchAndPrintTree);
      return traceback.output;
    }
    if (self.desc.equals("*")) {
      ClassMap.INSTANCE.classOutlines.get(self.owner).methods.stream()
          .filter(x -> x.methodName.equals(self.methodName) && x.owner.equals(self.owner))
          .forEach(traceback::searchAndPrintTree);
    }
    return traceback.output;
  }

  void searchAndPrintTree(Method self) {
    TreeNode root = searchTree(self, null, false);
    if (!root.callers.isEmpty()) printTree(root, 0);
  }

  TreeNode searchTree(Method self, TreeNode parent, boolean asSuper) {
    TreeNode cur = new TreeNode();
    cur.self = self;
    cur.isCallingSuper = asSuper;
    cur.parent = parent;
    searchCallers(self, cur, false);
    for (Method superMethod: ClassMap.INSTANCE.findSuperMethods(self)) {
      searchCallers(superMethod, cur, true);
    }
    return cur;
  }

  private void searchCallers(Method self, TreeNode cur, boolean asSuper) {
    CallerCollector callerCollector = GreatMap.INSTANCE.getCallerCollector(self);
    for (Method caller : callerCollector.getCallers()) {
      if (cur.findCycle(caller)) {
        TreeNode cycleEnd = new TreeNode();
        cycleEnd.self = caller;
        cur.callers.add(cycleEnd);
      } else {
        cur.callers.add(searchTree(caller, cur, asSuper));
      }
    }
  }


  void printTree(TreeNode node, int depth) {
    printer.print(node, depth);
    node.callers.forEach(c -> printTree(c, depth + 1));
  }

  static class TreeNode {
    Method self;
    boolean isCallingSuper = false; // self is calling the super method of parent
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

  private interface Printer {
    void print(TreeNode node, int depth);
  }
}
