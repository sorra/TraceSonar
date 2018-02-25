package sorra.tracesonar.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import sorra.tracesonar.model.Method;

public class Traceback {
  private boolean isHtml;
  private boolean includePotentialCalls;

  private Printer printer;
  private StringBuilder output = new StringBuilder();

  public Traceback(boolean isHtml, boolean includePotentialCalls) {
    this.isHtml = isHtml;
    this.includePotentialCalls = includePotentialCalls;

    if (isHtml) {
      printer = node -> {
        if (node.depth == 0) {
          output.append(String.format(
              "<div class=\"queried\">%s</div>\n", node.self));
        } else {
          String cssClass = "caller";
          if (node.callers.isEmpty()) cssClass += " endpoint";
          if (node.isCallingSuper) cssClass += " potential";

          output.append(String.format(
              "<div class=\"%s\" style=\"margin-left:%dem\">%s</div>\n", cssClass, node.depth * 5, node.self));
        }
      };
    } else {
      printer = node -> {
        char[] indents = new char[node.depth];
        Arrays.fill(indents, '\t');
        output.append(String.valueOf(indents)).append(node.self).append('\n');
      };
    }
  }

  public CharSequence run(Method self) {
    if (isHtml) output.append("<h3>").append(self).append("</h3>\n");
    else output.append(self).append("\n");

    search(self);

    return output;
  }

  private void search(Method self) {
    Stream<TreeNode> nodeStream;
    if (self.owner.equals("*")) {
      nodeStream = ClassMap.INSTANCE.classOutlines.values().stream()
          .flatMap(co -> co.methods.stream())
          .map(this::searchTree);
    } else if (self.methodName.equals("*")) {
      nodeStream = getClassOutline(self).methods.stream()
          .filter(x -> x.owner.equals(self.owner))
          .map(this::searchTree);
    } else if (self.desc.equals("*")) {
      nodeStream = getClassOutline(self).methods.stream()
          .filter(x -> x.methodName.equals(self.methodName) && x.owner.equals(self.owner))
          .map(this::searchTree);
    } else {
      throw new RuntimeException();
    }

    //Separate two stages to help debug
    nodeStream
        .collect(Collectors.toList())
        .forEach(this::printTree);
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

  private TreeNode searchTree(Method self, TreeNode parent, boolean asSuper) {
    TreeNode cur = new TreeNode(self, asSuper, parent);
    searchCallers(cur, false);

    //TODO if cur.depth < k (configurable)
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

  private void printTree(TreeNode node) {
    printer.print(node);
    node.callers.forEach(this::printTree);
  }

  private static class TreeNode {
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

  private interface Printer {
    void print(TreeNode node);
  }
}
