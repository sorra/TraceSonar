package sorra.tracesonar.core;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import sorra.tracesonar.model.Method;

/**
 * Trace-back Searcher
 */
class Searcher {
  private final boolean includePotentialCalls;
  private final boolean onlySearchDirectCalls;
  private final Collection<String> ends;
  private boolean stopped;

  Searcher(boolean includePotentialCalls, boolean onlySearchDirectCalls, Collection<String> ends) {
    this.includePotentialCalls = includePotentialCalls;
    this.onlySearchDirectCalls = onlySearchDirectCalls;
    this.ends = ends;
  }

  Stream<TreeNode> search(Method criteria) {
    List<Method> methods;
    ClassMap classMap = ClassMap.INSTANCE;
    if (criteria.owner.equals("*")) {
      methods = classMap.allClassOutlines().stream()
          .flatMap(co -> co.getMethods().stream())
          .collect(Collectors.toList());
    } else if (criteria.methodName.equals("*")) {
      methods = classMap.getClassOutline(criteria.owner).getMethods().stream()
          .filter(x -> x.owner.equals(criteria.owner))
          .collect(Collectors.toList());
    } else { // if (criteria.desc.equals("*")) {
      methods = classMap.getClassOutline(criteria.owner).getMethods().stream()
          .filter(x -> x.methodName.equals(criteria.methodName) && x.owner.equals(criteria.owner))
          .collect(Collectors.toList());
//    } else {
//      throw new IllegalArgumentException("invalid pattern");
    }

    return searchOnMethods(criteria, methods);
  }

  private Stream<TreeNode> searchOnMethods(Method criteria, Collection<Method> methods) {
    if (methods.isEmpty()) {
      throw new IllegalArgumentException("No method is found by given criteria " + criteria);
    }

    return methods.stream().map(this::searchTree);
  }

  private TreeNode searchTree(Method method) {
    return searchTree(method, null, false);
  }

  private TreeNode searchTree(Method self, TreeNode parent, boolean asSuper) {
    TreeNode cur = new TreeNode(self, asSuper, parent);
    if (cur.depth == 100) {
      cur.setError("Exceeds max depth!");
      stopped = true;
      return cur;
    }

    if (stopped || ends.stream().anyMatch(QualifierFilter.classQnameMatcher(self.owner))) {
      cur.setError("Stopped!");
      return cur;
    }

    if (onlySearchDirectCalls && cur.depth == 1) {
      return cur;
    }

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
//    if (cur.parent != null) {
//      if (cur.parent.findCycle(cur.self)) {
////        cur.parent.addCycleEnd(cur.self, asSuper);
//        return;
//      } else {
////        cur.parent.callers.add(cur);
//      }
//    }

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
