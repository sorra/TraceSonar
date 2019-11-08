package sorra.tracesonar.sample;

public class Subclass extends SuperClass {
  @Override
  public String does() {
    return super.does();
  }

  void call() {
    ((Interface)this).does();
    ((SuperClass)this).does();
  }
}
