package sorra.tracesonar.main;

import java.io.IOException;

public class SampleRun {
  public static void main(String[] args) throws IOException {
    Main.main("-p -f target/classes -q sorra.tracesonar.sample.BridgedClass sorra.tracesonar.sample.Subclass".split(" "));
  }
}
