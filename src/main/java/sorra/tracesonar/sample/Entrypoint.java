package sorra.tracesonar.sample;

public class Entrypoint {
  public static void main(String[] args) {
    recursive(2);
  }

  private static void recursive(int times) {
    if (times != 0) {
      new Subclass().call();
      recursive(--times);
    }
  }
}
