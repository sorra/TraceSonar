package sorra.tracesonar.core;

import java.util.Set;

import sorra.tracesonar.model.Caller;

public class CallerCollector {
  private String className;
  private Set<Caller> callers = Factory.infoSet();

  public CallerCollector(String className) {
    this.className = className;
  }

  public void regCaller(Caller caller) {
    callers.add(caller);
  }

  public Set<Caller> getCallers() {
    return callers;
  }
}
