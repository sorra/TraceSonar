package sorra.tracesonar.sample.inherit;

public class SuperClass implements Interface {
  @Override
  public Object does() {
    return null;
  }

  private void call() {
    ((Interface)this).does();
  }
}
