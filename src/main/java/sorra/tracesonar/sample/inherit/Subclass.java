package sorra.tracesonar.sample.inherit;

public class Subclass extends SuperClass {
  @Override
  public Object does() {
    return new Object();
  }

  private void call() {
    ((Interface)this).does();
    ((SuperClass)this).does();
  }
}
