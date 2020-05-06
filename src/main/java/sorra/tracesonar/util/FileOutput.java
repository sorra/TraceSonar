package sorra.tracesonar.util;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FileOutput {
  public static void writeFile(String filename, CharSequence content) {
    try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_8)) {
      writer.append(content);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
