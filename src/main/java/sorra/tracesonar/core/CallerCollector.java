package sorra.tracesonar.core;

import java.util.Set;

import sorra.tracesonar.model.Method;

public class CallerCollector {
  private Method self;
  private Set<Method> callers = Factory.infoSet();

  public CallerCollector(Method self) {
    this.self = self;
  }

  public void addCaller(Method caller) {
    callers.add(caller);
  }

  public Set<Method> getCallers() {
    return callers;
  }
}
