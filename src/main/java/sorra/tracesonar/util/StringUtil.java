package sorra.tracesonar.util;

public abstract class StringUtil {
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

  public static String[] splitFirst(final String str, final String separator) {
    if (str == null || str.isEmpty() || separator.isEmpty()) {
      return new String[]{str};
    }
    final int pos = str.indexOf(separator);
    if (pos < 0) {
      return new String[]{str};
    }
    return new String[]{str.substring(0, pos),
        str.substring(pos + separator.length(), str.length())};
  }

  public static String removeSurrounding(String str, String piece) {
    if (str.length() == 0 || piece.length() == 0) {
      return str;
    }

    boolean changed = false;

    int beginIdx = 0;
    if (str.startsWith(piece)) {
      beginIdx += piece.length();
      changed = true;
    }

    if (beginIdx >= str.length()) {
      return "";
    }

    int endIdx = str.length();
    if (str.endsWith(piece)) {
      endIdx -= piece.length();
      changed = true;
    }

    return changed ? str.substring(beginIdx, endIdx) : str;
  }
}
