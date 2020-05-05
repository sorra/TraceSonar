package sorra.tracesonar.model;

import java.util.Objects;

/**
 * Represents a criteria to compare with method identifier
 */
public class Query {
  public final String owner;
  public final String methodName;
  public final String params;

  public Query(String owner, String methodName, String params) {
    Objects.requireNonNull(owner);
    Objects.requireNonNull(methodName);
    Objects.requireNonNull(params);
    this.owner = owner;
    this.methodName = methodName;
    this.params = params;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Query query = (Query) o;
    return owner.equals(query.owner) &&
        methodName.equals(query.methodName) &&
        params.equals(query.params);
  }

  @Override
  public int hashCode() {
    return Objects.hash(owner, methodName, params);
  }

  @Override
  public String toString() {
    return "Query{" +
        "owner='" + owner + '\'' +
        ", methodName='" + methodName + '\'' +
        ", params='" + params + '\'' +
        '}';
  }
}
