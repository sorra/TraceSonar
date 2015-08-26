package sorra.tracesonar.model;

public class Caller {
  public final String owner;
  public final String methodName;
  public final String desc;

  public Caller(String owner, String methodName, String desc) {
    this.owner = owner;
    this.methodName = methodName;
    this.desc = desc;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Caller caller = (Caller) o;

    if (!methodName.equals(caller.methodName)) return false;
    if (!owner.equals(caller.owner)) return false;
    return desc.equals(caller.desc);

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
    return "{" + owner + ' ' + methodName + ' ' + desc + '}';
  }
}
