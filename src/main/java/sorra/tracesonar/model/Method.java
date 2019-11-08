package sorra.tracesonar.model;

/**
 * Represents a method identifier in class files
 */
public class Method {
  public final String owner;
  public final String methodName;
  public final String desc;

  public Method(String owner, String methodName, String desc) {
    this.owner = owner;
    this.methodName = methodName;
    this.desc = desc;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Method caller = (Method) o;

    return methodName.equals(caller.methodName) && owner.equals(caller.owner) && desc.equals(caller.desc);
  }

  @Override
  public int hashCode() {
    int result = owner.hashCode();
    result = 31 * result + methodName.hashCode();
    // result = 31 * result + desc.hashCode();
    return result;
  }

  @Override
  public String toString() {
    String name = methodName;
    if (methodName.contains("<") || methodName.contains(">")) {
      name = methodName.replace("<", "&lt;").replace(">", "&gt;");
    }

    String desc = this.desc.replace("Ljava/lang/", "");

    return String.format("<- %s #%s %s", owner.replace('/', '.'), name, desc);
  }
}
