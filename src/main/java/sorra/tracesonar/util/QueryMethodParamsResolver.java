package sorra.tracesonar.util;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Stateless resolver
 */
public class QueryMethodParamsResolver {

  public List<String> resolve(String input) {
    return Stream.of(input.split(","))
        .map(String::trim)
        .filter(p -> !p.isEmpty())
        .collect(Collectors.toList());
  }
}
