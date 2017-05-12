package sorra.tracesonar.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import sorra.tracesonar.model.Method;

public class Traceback {
  private StringBuilder output = new StringBuilder();
  private Printer printer;

  public static CharSequence run(Method self, boolean html) {
    Traceback traceback = getInstance(self, html);

    search(self, traceback);

    return traceback.output;
  }

  private static Traceback getInstance(Method self, boolean html) {
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
    return traceback;
  }

  private static void search(Method self, Traceback traceback) {
    Stream<TreeNode> nodeStream;
    if (self.owner.equals("*")) {
      nodeStream = ClassMap.INSTANCE.classOutlines.values().stream()
          .flatMap(co -> co.methods.stream())
          .map(traceback::searchTree);
    } else if (self.methodName.equals("*")) {
      nodeStream = ClassMap.INSTANCE.classOutlines.get(self.owner).methods.stream()
          .filter(x -> x.owner.equals(self.owner))
          .map(traceback::searchTree);
    } else if (self.desc.equals("*")) {
      nodeStream = ClassMap.INSTANCE.classOutlines.get(self.owner).methods.stream()
          .filter(x -> x.methodName.equals(self.methodName) && x.owner.equals(self.owner))
          .map(traceback::searchTree);
    } else {
      throw new RuntimeException();
    }

    //Separate two stages to help debug
    nodeStream
        .collect(Collectors.toList())
        .forEach(traceback::printTree);
  }

  TreeNode searchTree(Method method) {
    return searchTree(method, null, false);
  }

  private void printTree(TreeNode root) {
    if (!root.callers.isEmpty()) printTree(root, 0);
  }

  private TreeNode searchTree(Method self, TreeNode parent, boolean asSuper) {
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


  private void printTree(TreeNode node, int depth) {
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
