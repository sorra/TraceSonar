package sorra.tracesonar.util;

import java.io.*;

public class FileOutput {
  public static void writeFile(String filename, CharSequence content) {
    try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(filename), "UTF-8")) {
      writer.append(content);
      writer.close();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
