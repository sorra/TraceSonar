package sorra.tracesonar.main;

import sorra.tracesonar.core.FileWalker;
import sorra.tracesonar.core.GreatMap;
import sorra.tracesonar.core.Traceback;
import sorra.tracesonar.model.Method;

public class Main {
  public static void main(String[] args) {
//    for (String root : args) {
//      FileWalker.walkAll(root);
//    }
    FileWalker.walkAll("target");

    System.out.println("Collected callers:\n");
    GreatMap.INSTANCE.callerCollectors.forEach((s, callerCollector) -> {
      System.out.printf("%s callers: %s\n", s, callerCollector.getCallers().size());
      callerCollector.getCallers().forEach(caller -> System.out.println("\t" + caller));
    });

    System.out.println("\nTraceback:\n");
    Traceback.search(new Method("sorra/tracesonar/core/GreatMap", "*", ""));

  }
}
