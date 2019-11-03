package sorra.tracesonar.core;

import java.util.Map;

import sorra.tracesonar.model.Method;

public class GraphStore {
  public static final GraphStore INSTANCE = new GraphStore();

  private Map<Method, CallerCollector> callerCollectors = Factory.infoMap();

  public CallerCollector getCallerCollector(Method callee) {
    return callerCollectors.computeIfAbsent(callee, CallerCollector::new);
  }
}
