package sorra.tracesonar.main;

import java.util.*;

public final class ArgsParser {
  private String[] args;

  private int i;

  private Map<Option, List<String>> optionValuesMap = new HashMap<>();

  public ArgsParser(String[] args) {
    if (args.length == 0) {
      throw new IllegalArgumentException("No CLI arguments!");
    }
    this.args = args;
    parse();
  }

  public List<String> getOptionValues(Option option) {
    List<String> values = optionValuesMap.get(option);
    return values != null ? values : Collections.emptyList();
  }

  private void parse() {
    for (i = 0; i < args.length; i++) {
      String arg = args[i];
      if (!arg.startsWith("-")) {
        throw new IllegalArgumentException("Unknow CLI argument: " + arg);
      }
      switch (arg) {
        case "-f":
        case "--file":
          saveValues(Option.FILE);
          break;
        case "-q":
        case "--query":
          saveValues(Option.QUERY);
          break;
        case "-p":
        case "--potential":
          optionValuesMap.put(Option.POTENTIAL, Collections.singletonList("true"));
        case "--exclude":
          saveValues(Option.EXCLUDE);
          break;
        case "--include-only":
          saveValues(Option.INCLUDE_ONLY);
          break;
        case "--stop-at":
          saveValues(Option.STOP_AT);
          break;
        default:
          throw new IllegalArgumentException("Unknown CLI option: " + arg);
      }
    }
  }

  private void saveValues(Option query) {
    optionValuesMap.put(query, readValues());
  }

  private List<String> readValues() {
    List<String> values = new ArrayList<>();

    while (true) {
      i++;
      if (i < args.length && !args[i].startsWith("-")) {
        values.add(args[i]);
      } else {
        i--;
        break;
      }
    }

    return values;
  }

  public enum Option {
    FILE, QUERY, POTENTIAL, EXCLUDE, INCLUDE_ONLY, STOP_AT;
  }
}
