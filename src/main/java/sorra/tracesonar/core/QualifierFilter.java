package sorra.tracesonar.core;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * Filters classes by specified qualifiers and a matcher predicate.
 * Effective like:
 * filtered = (all & included) - excluded
 */
public class QualifierFilter {
  private Collection<String> includedQualifiers;
  private Collection<String> excludedQualifiers;

  public QualifierFilter(Collection<String> includedQualifiers, Collection<String> excludedQualifiers) {
    this.includedQualifiers = includedQualifiers;
    this.excludedQualifiers = excludedQualifiers;
  }

  public boolean filter(Predicate<String> qualifierMatcher) {
    boolean included = includedQualifiers.isEmpty() || includedQualifiers.stream().anyMatch(qualifierMatcher);
    return included && excludedQualifiers.stream().noneMatch(qualifierMatcher);
  }
}
