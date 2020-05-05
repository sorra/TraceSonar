package sorra.tracesonar.main;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import sorra.tracesonar.core.FileWalker;
import sorra.tracesonar.core.QualifierFilter;
import sorra.tracesonar.core.Traceback;
import sorra.tracesonar.model.Query;
import sorra.tracesonar.util.FileOutput;
import sorra.tracesonar.util.FileUtil;
import sorra.tracesonar.util.StringUtil;

public class Main {
  public static void main(String[] args) throws IOException {
    ArgsParser parser = new ArgsParser(args);
    System.out.println(parser.prettyPrint());

    buildDatabase(parser);

    StringBuilder allOutput = new StringBuilder();
    {
      long timeStart = System.currentTimeMillis();

      boolean potential = parser.getOptionValues(ArgsParser.Option.POTENTIAL).contains("true");
      boolean direct = parser.getOptionValues(ArgsParser.Option.DIRECT).contains("true");
      List<String> queries = parser.getOptionValues(ArgsParser.Option.QUERY);
      List<String> ends = translateQnames(parser.getOptionValues(ArgsParser.Option.END_AT));

      for (String query : queries) {
        // Format: qualifier.Class#method()
        String[] classAndMethod = StringUtil.splitFirst(query, "#");
        String qClassName = classAndMethod[0].replace('.', '/');
        String methodName;
        String params;
        if (classAndMethod.length > 2) {
          throw new IllegalArgumentException(query);
        } else if (classAndMethod.length < 2) {
          methodName = "*";
          params = "*";
        } else {
          String[] methodAndParams = StringUtil.splitFirst(classAndMethod[1], "(");
          methodName = methodAndParams[0];
          if (methodAndParams.length > 2) {
            throw new IllegalArgumentException(query);
          } else if (methodAndParams.length < 2) {
            params = "*";
          } else {
            params = methodAndParams[1];
            if (params.endsWith(")")) {
              params = params.substring(0, params.length() - 1);
            }
          }
        }

        CharSequence output = new Traceback(true, potential, direct).run(new Query(qClassName, methodName, params), ends);
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

  private static void buildDatabase(ArgsParser parser) {
    long timeStart = System.currentTimeMillis();

    List<String> files = parser.getOptionValues(ArgsParser.Option.FILE);
    List<String> includes = translateQnames(parser.getOptionValues(ArgsParser.Option.INCLUDE));
    List<String> excludes = translateQnames(parser.getOptionValues(ArgsParser.Option.EXCLUDE));
    FileWalker.walkAll(files, new QualifierFilter(includes, excludes));

    long timeCost = System.currentTimeMillis() - timeStart;
    System.out.println("Walk time cost: " + timeCost);
  }

  private static List<String> translateQnames(List<String> qnames) {
    return qnames.stream()
        .map(x -> x.replace('.', '/'))
        .collect(Collectors.toList());
  }
}
