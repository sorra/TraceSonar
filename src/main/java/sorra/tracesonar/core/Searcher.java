package sorra.tracesonar.core;

import java.util.Set;
import java.util.stream.Stream;

import sorra.tracesonar.model.Method;

/**
 * Trace-back Searcher
 */
class Searcher {
  private boolean includePotentialCalls;

  Searcher(boolean includePotentialCalls) {
    this.includePotentialCalls = includePotentialCalls;
  }

  Stream<TreeNode> search(Method self) {
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
      throw new IllegalArgumentException("invalid pattern");
    }

    return nodeStream;
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
      if (cur.parent.findCycle(cur.self)) {
        cur.parent.addCycleEnd(cur.self, asSuper);
        return;
      } else {
        cur.parent.callers.add(cur);
      }
    }

    Set<Method> callers = GraphStore.INSTANCE.getCallerCollector(cur.self).getCallers();
    for (Method caller : callers) {
      if (cur.findCycle(caller)) {
        cur.addCycleEnd(caller, asSuper);
      } else {
        searchTree(caller, cur, asSuper);
      }
    }
  }
}
