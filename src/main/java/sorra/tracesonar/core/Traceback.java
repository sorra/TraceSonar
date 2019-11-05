package sorra.tracesonar.core;

import java.util.Arrays;
import java.util.stream.Collectors;

import sorra.tracesonar.model.Method;

/**
 * Trace-back search and print the result tree
 */
public class Traceback {
  private boolean isHtml;
  private boolean includePotentialCalls;

  private Printer printer;
  private StringBuilder output = new StringBuilder();

  public Traceback(boolean isHtml, boolean includePotentialCalls) {
    this.isHtml = isHtml;
    this.includePotentialCalls = includePotentialCalls;

    if (isHtml) {
      printer = node -> {
        if (node.depth == 0) {
          output.append(String.format(
              "<div class=\"queried\">%s</div>\n", node.self));
        } else {
          String cssClass = "caller";
          if (node.callers.isEmpty()) cssClass += " endpoint";
          if (node.isCallingSuper) cssClass += " potential";

          output.append(String.format(
              "<div class=\"%s\" style=\"margin-left:%dem\">%s</div>\n", cssClass, node.depth * 5, node.self));
        }
      };
    } else {
      printer = node -> {
        char[] indents = new char[node.depth];
        Arrays.fill(indents, '\t');
        output.append(String.valueOf(indents)).append(node.self).append('\n');
      };
    }
  }

  public CharSequence run(Method self) {
    if (isHtml) output.append("<h3>").append(self).append("</h3>\n");
    else output.append(self).append("\n");

    // Though java.util.Stream can be lazy
    // Still separate two stages to help debug
    new Searcher(includePotentialCalls).search(self)
        .collect(Collectors.toList())
        .forEach(this::printTree);

    return output;
  }

  private void printTree(TreeNode node) {
    printer.print(node);
    node.callers.forEach(this::printTree);
  }

  private interface Printer {
    void print(TreeNode node);
  }
}
