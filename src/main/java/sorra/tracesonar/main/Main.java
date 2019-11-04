package sorra.tracesonar.main;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import sorra.tracesonar.core.FileWalker;
import sorra.tracesonar.core.QualifierFilter;
import sorra.tracesonar.core.Traceback;
import sorra.tracesonar.model.Method;
import sorra.tracesonar.util.FileOutput;
import sorra.tracesonar.util.FileUtil;
import sorra.tracesonar.util.StringUtil;

public class Main {
  public static void main(String[] args) throws IOException {
    ArgsParser parser = new ArgsParser(args);


    {
      long timeStart = System.currentTimeMillis();

      List<String> files = parser.getOptionValues(ArgsParser.Option.FILE);
      List<String> includes = translateQnames(parser.getOptionValues(ArgsParser.Option.INCLUDE));
      List<String> excludes = translateQnames(parser.getOptionValues(ArgsParser.Option.EXCLUDE));
      FileWalker.walkAll(files, new QualifierFilter(includes, excludes));

      long timeCost = System.currentTimeMillis() - timeStart;
      System.out.println("Walk time cost: " + timeCost);
    }

    boolean potential = parser.getOptionValues(ArgsParser.Option.POTENTIAL).contains("true");
    System.out.println("potential=" + potential);

    List<String> queries = parser.getOptionValues(ArgsParser.Option.QUERY);

    StringBuilder allOutput = new StringBuilder();
    {
      long timeStart = System.currentTimeMillis();

      for (String query : queries) {
        // qualifiedName#method
        String[] parts = StringUtil.splitFirst(query, "#");
        String qClassName = parts[0].replace('.', '/');
        String methodName = parts.length >= 2 ? parts[1] : "*";
        CharSequence output = new Traceback(true, potential).run(new Method(qClassName, methodName, "*"));
        allOutput.append(output);
      }

      long timeCost = System.currentTimeMillis() - timeStart;
      System.out.println("Search time cost: " + timeCost);
    }

    InputStream tmplInput = Main.class.getClassLoader().getResourceAsStream("traceback.html");
    if (tmplInput == null) throw new NullPointerException("tmpl file is not found");
    String tmpl = new String(FileUtil.read(tmplInput, 300), StandardCharsets.UTF_8);
    FileOutput.writeFile("traceback.html", String.format(tmpl, allOutput));
    System.out.println("\nTraceback: traceback.html\n");
  }

  private static List<String> translateQnames(List<String> qnames) {
    return qnames.stream()
        .map(x -> x.replace('.', '/'))
        .collect(Collectors.toList());
  }
}
