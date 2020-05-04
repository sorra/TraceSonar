package sorra.tracesonar.sample.modifier;

class Private implements Interface {

  @Override
  public void publicMethod() {
    privateMethod();
  }

  private void privateMethod() {
    protectedMethod();
  }

  protected void protectedMethod() {
    System.out.println();
  }
}
