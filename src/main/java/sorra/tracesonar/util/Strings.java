package sorra.tracesonar.util;

public abstract class Strings {
  public static String substringBefore(final String str, final String separator) {
    if (str == null || str.isEmpty()) {
      return str;
    }
    if (separator.isEmpty()) {
      return "";
    }
    final int pos = str.indexOf(separator);
    if (pos < 0) {
      return str;
    }
    return str.substring(0, pos);
  }
}
