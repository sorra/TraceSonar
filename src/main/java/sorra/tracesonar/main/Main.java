package sorra.tracesonar.main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import sorra.tracesonar.core.FileWalker;
import sorra.tracesonar.core.GreatMap;
import sorra.tracesonar.core.Traceback;
import sorra.tracesonar.model.Method;
import sorra.tracesonar.util.FileOutput;

public class Main {
  public static void main(String[] args) throws IOException {
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
    CharSequence output = Traceback.search(new Method("sorra/tracesonar/core/GreatMap", "*", ""), true);
    System.out.println(output);
    String tmplFileName = Main.class.getClassLoader().getResource("traceback.html").getFile();
    String tmpl = new String(Files.readAllBytes(Paths.get(tmplFileName)), "UTF-8");
    FileOutput.writeFile("traceback.html", String.format(tmpl, output));
  }
}
