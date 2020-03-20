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

  public boolean filterClass(String classQname) {
    return filter(classQnameMatcher(classQname));
  }

  public boolean filterClassFile(String path) {
    return filter(classFileMatcher(path));
  }

  private boolean filter(Predicate<String> qualifierMatcher) {
    boolean included = includedQualifiers.isEmpty() || includedQualifiers.stream().anyMatch(qualifierMatcher);
    return included && excludedQualifiers.stream().noneMatch(qualifierMatcher);
  }

  public static Predicate<String> classQnameMatcher(String classQname) {
    return q ->
        classQname.equals(q) || classQname.startsWith(q + '/') || classQname.startsWith(q + '$');
  }

  public static Predicate<String> classFileMatcher(String path) {
    return q ->
        path.endsWith(q + ".class") || path.contains(q + '/') || path.contains(q + '$');
  }
}
