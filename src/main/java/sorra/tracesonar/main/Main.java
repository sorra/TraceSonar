package sorra.tracesonar.main;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import sorra.tracesonar.core.FileWalker;
import sorra.tracesonar.core.Traceback;
import sorra.tracesonar.model.Method;
import sorra.tracesonar.util.FileOutput;
import sorra.tracesonar.util.FileUtil;
import sorra.tracesonar.util.Strings;

public class Main {
  public static void main(String[] args) throws IOException {
    Option option = null;
    List<String> files = new ArrayList<>();
    List<String> queries = new ArrayList<>();
    boolean potential = true;
    List<String> ignores = new ArrayList<>();

    for (String arg : args) {
      arg = arg.trim();
      if (arg.equals("-f")) option = Option.FILE;
      else if (arg.equals("-q")) option = Option.QUERY;
      else if (arg.equals("-p")) option = Option.POTENTIAL;
      else if (arg.equals("-i")) option = Option.IGNORE;
      else {
        if (option == Option.FILE) files.add(arg);
        else if (option == Option.QUERY) queries.add(arg);
        else if (option == Option.POTENTIAL) potential = Boolean.valueOf(arg);
        else if (option == Option.IGNORE) ignores.add(arg.replace('.', '/'));
      }
    }
    FileWalker.walkAll(files, ignores);

//    System.out.println("Collected callers:\n");
//    GreatMap.INSTANCE.callerCollectors.forEach((s, callerCollector) -> {
//      System.out.printf("%s callers: %s\n", s, callerCollector.getCallers().size());
//      callerCollector.getCallers().forEach(caller -> System.out.println("\t" + caller));
//    });

    StringBuilder allsb = new StringBuilder();
    for (String query : queries) {
      String[] parts = Strings.splitFirst(query, "#"); // qualifiedName#method
      String qClassName = parts[0].replace('.', '/');
      String methodName = parts.length >= 2 ? parts[1] : "*";
      CharSequence output = Traceback.getInstance(potential, true).run(new Method(qClassName, methodName, "*"));
      allsb.append(output);
    }

    InputStream tmplInput = Main.class.getClassLoader().getResourceAsStream("traceback.html");
    if (tmplInput == null) throw new NullPointerException("tmpl file is not found");
    String tmpl = new String(FileUtil.read(tmplInput, 300), "UTF-8");
    FileOutput.writeFile("traceback.html", String.format(tmpl, allsb));
    System.out.println("\nTraceback: traceback.html\n");
  }

  enum Option {
    FILE, QUERY, POTENTIAL, IGNORE
  }
}
