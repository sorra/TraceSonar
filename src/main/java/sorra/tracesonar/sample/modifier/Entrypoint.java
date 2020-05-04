package sorra.tracesonar.sample.modifier;

public class Entrypoint {

  public static void main(String[] args) {
    Interface p = new Private();
    p.publicMethod();
  }
}
