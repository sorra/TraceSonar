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
    Mode mode = null;
    List<String> files = new ArrayList<>();
    List<String> queries = new ArrayList<>();
    for (String arg : args) {
      if (arg.equals("-f")) mode = Mode.FILE;
      else if(arg.equals("-q")) mode = Mode.QUERY;
      else {
        if (mode == Mode.FILE) files.add(arg);
        else if(mode == Mode.QUERY) queries.add(arg.replace("'", ""));
      }
    }
    FileWalker.walkAll(files);

//    System.out.println("Collected callers:\n");
//    GreatMap.INSTANCE.callerCollectors.forEach((s, callerCollector) -> {
//      System.out.printf("%s callers: %s\n", s, callerCollector.getCallers().size());
//      callerCollector.getCallers().forEach(caller -> System.out.println("\t" + caller));
//    });

    StringBuilder allsb = new StringBuilder();
    for (String query : queries) {
      String[] parts = Strings.splitFirst(query, "#"); // qualifiedName#method
      CharSequence output = Traceback.run(new Method(parts[0].replace('.', '/'), parts[1], ""), true);
      allsb.append(output);
    }

    InputStream tmplInput = Main.class.getClassLoader().getResourceAsStream("traceback.html");
    if (tmplInput == null) throw new NullPointerException("tmpl file is not found");
    String tmpl = new String(FileUtil.read(tmplInput, 300), "UTF-8");
    FileOutput.writeFile("traceback.html", String.format(tmpl, allsb));
    System.out.println("\nTraceback: traceback.html\n");
  }

  enum Mode {
    FILE, QUERY
  }
}
