package sorra.tracesonar.main;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import sorra.tracesonar.core.FileWalker;
import sorra.tracesonar.core.Traceback;
import sorra.tracesonar.model.Method;
import sorra.tracesonar.util.FileOutput;
import sorra.tracesonar.util.FileUtil;
import sorra.tracesonar.util.StringUtil;

public class Main {
  public static void main(String[] args) throws IOException {
    ArgsParser parser = new ArgsParser(args);

    List<String> files = parser.getOptionValues(ArgsParser.Option.FILE);
    List<String> excludes = parser.getOptionValues(ArgsParser.Option.EXCLUDE)
        .stream()
        .map(x -> x.replace('.', '/'))
        .collect(Collectors.toList());

    FileWalker.walkAll(files, excludes);

//    System.out.println("Collected callers:\n");
//    GreatMap.INSTANCE.callerCollectors.forEach((s, callerCollector) -> {
//      System.out.printf("%s callers: %s\n", s, callerCollector.getCallers().size());
//      callerCollector.getCallers().forEach(caller -> System.out.println("\t" + caller));
//    });

    boolean potential = parser.getOptionValues(ArgsParser.Option.POTENTIAL).contains("true");
    List<String> queries = parser.getOptionValues(ArgsParser.Option.QUERY);

    StringBuilder allsb = new StringBuilder();
    for (String query : queries) {
      // qualifiedName#method
      String[] parts = StringUtil.splitFirst(StringUtil.removeSurrounding(query, "."), "#");
      String qClassName = parts[0].replace('.', '/');
      String methodName = parts.length >= 2 ? parts[1] : "*";
      CharSequence output = new Traceback(potential, true).run(new Method(qClassName, methodName, "*"));
      allsb.append(output);
    }

    InputStream tmplInput = Main.class.getClassLoader().getResourceAsStream("traceback.html");
    if (tmplInput == null) throw new NullPointerException("tmpl file is not found");
    String tmpl = new String(FileUtil.read(tmplInput, 300), "UTF-8");
    FileOutput.writeFile("traceback.html", String.format(tmpl, allsb));
    System.out.println("\nTraceback: traceback.html\n");
  }
}
