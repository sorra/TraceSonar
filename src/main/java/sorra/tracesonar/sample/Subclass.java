package sorra.tracesonar.sample;

public class Subclass extends SuperClass {
  @Override
  public void does() {
    super.does();
  }

  void metal() {
    ((Interface)this).does();
    ((SuperClass)this).does();
  }
}
