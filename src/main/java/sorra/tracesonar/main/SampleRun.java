package sorra.tracesonar.main;

import java.io.IOException;

public class SampleRun {
  public static void main(String[] args) throws IOException {
    String params = "--potential -f target/classes -q sorra.tracesonar.sample.modifier.Private";
    Main.main(params.split(" "));
  }
}
