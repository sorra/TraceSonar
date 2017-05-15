package sorra.tracesonar.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import sorra.tracesonar.model.Method;

public class Traceback {
  private StringBuilder output = new StringBuilder();
  private boolean isHtml;
  private Printer printer;
  private boolean includePotentialCalls;

  public CharSequence run(Method self) {
    if (isHtml) output.append("<h3>").append(self).append("</h3>\n");
    else output.append(self).append("\n");

    search(self, this);

    return output;
  }

  public static Traceback getInstance(boolean includePotentialCalls, boolean html) {
    Traceback traceback = new Traceback();
    traceback.isHtml = true;
    traceback.includePotentialCalls = includePotentialCalls;

    if (html) {
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
      nodeStream = getClassOutline(self).methods.stream()
          .filter(x -> x.owner.equals(self.owner))
          .map(traceback::searchTree);
    } else if (self.desc.equals("*")) {
      nodeStream = getClassOutline(self).methods.stream()
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

  private static ClassMap.ClassOutline getClassOutline(Method self) {
    ClassMap.ClassOutline classOutline = ClassMap.INSTANCE.classOutlines.get(self.owner);
    if (classOutline == null) {
      throw new RuntimeException("Cannot find class: " + self.owner);
    }
    return  classOutline;
  }

  private TreeNode searchTree(Method method) {
    return searchTree(method, null, false);
  }

  private void printTree(TreeNode root) {
    if (!root.callers.isEmpty()) printTree(root, 0);
  }

  private TreeNode searchTree(Method self, TreeNode parent, boolean asSuper) {
    TreeNode cur = new TreeNode(self, asSuper, parent);
    searchCallers(cur, false);

    if (includePotentialCalls) {
      for (Method superMethod : ClassMap.INSTANCE.findSuperMethods(self)) {
        TreeNode superCur = new TreeNode(superMethod, asSuper, parent);
        searchCallers(superCur, true);
      }
    }

    return cur;
  }

  private void searchCallers(TreeNode cur, boolean asSuper) {
    if (cur.parent != null) {
      cur.parent.callers.add(cur);
    }

    Set<Method> callers = GreatMap.INSTANCE.getCallerCollector(cur.self).getCallers();
    for (Method caller : callers) {
      if (cur.findCycle(caller)) {
        TreeNode cycleEnd = new TreeNode(caller, asSuper, cur);
        cur.callers.add(cycleEnd);
      } else {
        searchTree(caller, cur, asSuper);
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

    public TreeNode(Method self, boolean isCallingSuper, TreeNode parent) {
      this.self = self;
      this.isCallingSuper = isCallingSuper;
      this.parent = parent;
    }

    boolean findCycle(Method neo) {
      if (self.equals(neo)) {
        return true;
      }

      TreeNode cur = parent;
      while (cur != null) {
        if (cur.self.equals(neo) || cur.callers.stream().anyMatch(x -> x.self.equals(neo))) {
          return true;
        }
        cur = cur.parent;
      }
      return false;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();

      TreeNode cur = this;
      int depth = 0;
      do {
        sb.append(depth);
        for (int i = 0; i < depth; i++) {
          sb.append("  ");
        }
        sb.append(cur.self).append('\n');
        cur = cur.parent;
        depth++;
      } while (cur != null);

      return sb.toString();
    }
  }

  private interface Printer {
    void print(TreeNode node, int depth);
  }
}
