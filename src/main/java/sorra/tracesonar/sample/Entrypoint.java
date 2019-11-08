package sorra.tracesonar.sample;

public class Entrypoint {
  public static void main(String[] args) {
    Interface object = new BridgedClass();
    object.does();
    recursive(2);
  }

  private static void recursive(int times) {
    if (times != 0) {
      Interface object = new Subclass();
      object.does();
      recursive(--times);
    }
  }
}
