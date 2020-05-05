package sorra.tracesonar.core;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import sorra.tracesonar.model.Method;
import sorra.tracesonar.model.Query;
import sorra.tracesonar.util.BytecodeMethodParamsResolver;
import sorra.tracesonar.util.QueryMethodParamsResolver;
import sorra.tracesonar.util.StringUtil;

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

  Stream<TreeNode> search(Query query) {
    List<Method> methods;
    ClassMap classMap = ClassMap.INSTANCE;
    if (query.owner.equals("*")) {
      methods = classMap.allClassOutlines().stream()
          .flatMap(co -> co.getMethods().stream())
          .collect(Collectors.toList());
    } else if (query.methodName.equals("*")) {
      methods = classMap.getClassOutline(query.owner).getMethods().stream()
          .filter(x -> x.owner.equals(query.owner))
          .collect(Collectors.toList());
    } else if (query.params.equals("*")) {
      methods = classMap.getClassOutline(query.owner).getMethods().stream()
          .filter(x -> x.methodName.equals(query.methodName) && x.owner.equals(query.owner))
          .collect(Collectors.toList());
    } else {
      methods = classMap.getClassOutline(query.owner).getMethods().stream()
          .filter(x -> x.methodName.equals(query.methodName) && x.owner.equals(query.owner) && paramsMatch(query, x))
          .collect(Collectors.toList());
    }

    if (methods.isEmpty()) {
      throw new IllegalArgumentException("invalid pattern: no matching target");
    }

    return searchOnMethods(methods);
  }

  private boolean paramsMatch(Query query, Method method) {
    List<String> queryParams = new QueryMethodParamsResolver().resolve(query.params);
    List<String> methodParams = new BytecodeMethodParamsResolver().resolve(method.desc);
    if (queryParams.size() != methodParams.size()) {
      return false;
    }

    int size = queryParams.size();
    for (int i = 0; i < size; i++) {
      String q = queryParams.get(i);
      String m = methodParams.get(i);

      if (m.endsWith(q)) {
        continue;
      }
      if (m.endsWith(q) && m.endsWith("." + q)) {
        continue;
      }
      return false;
    }

    return true;
  }

  private Stream<TreeNode> searchOnMethods(Collection<Method> methods) {
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

    if (stopped) {
      cur.setError("Stopped!");
      return cur;
    }

    if (ends.stream().anyMatch(QualifierFilter.classQnameMatcher(self.owner))) {
      cur.setError("Ended at this!");
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
