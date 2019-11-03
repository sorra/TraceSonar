package sorra.tracesonar.core;

import java.util.Map;
import java.util.Optional;

import sorra.tracesonar.model.Method;

public class GraphStore {
  public static final GraphStore INSTANCE = new GraphStore();

  private Map<Method, CallerCollector> callerCollectors = Factory.infoMap();
  private Map<String, MethodInsnCollector> methodInsnCollectors = Factory.infoMap();

  public CallerCollector getCallerCollector(Method callee) {
    return callerCollectors.computeIfAbsent(callee, CallerCollector::new);
  }

  public Optional<MethodInsnCollector> getMethodInsnCollector(String className) {
    return Optional.ofNullable(methodInsnCollectors.get(className));
  }

  public void addMethodInsnCollector(MethodInsnCollector methodInsnCollector) {
    methodInsnCollectors.put(methodInsnCollector.getClassName(), methodInsnCollector);
  }
}
