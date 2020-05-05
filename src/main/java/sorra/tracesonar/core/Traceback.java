package sorra.tracesonar.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import sorra.tracesonar.model.Query;

/**
 * Trace-back search and print the result tree
 */
public class Traceback {
  private final boolean isHtml;
  private final boolean includePotentialCalls;
  private final boolean onlySearchDirectCalls;

  private Printer printer;
  private StringBuilder output = new StringBuilder();

  public Traceback(boolean isHtml, boolean includePotentialCalls, boolean onlySearchDirectCalls) {
    this.isHtml = isHtml;
    this.includePotentialCalls = includePotentialCalls;
    this.onlySearchDirectCalls = onlySearchDirectCalls;

    if (isHtml) {
      printer = node -> {
        if (node.depth == 0) {
          output.append(String.format(
              "<div class=\"queried\">%s</div>\n", node.self));
        } else {
          String cssClass = "caller";
          if (node.callers.isEmpty()) cssClass += " endpoint";
          if (node.isCallingSuper) cssClass += " potential";

          String errorMessage;
          if (node.hasError()) {
            cssClass += " error";
            errorMessage = " {" + node.getError() + "}";
          }
          else errorMessage = "";

          output.append(String.format(
              "<div class=\"%s\" style=\"margin-left:%dem\">%s%s</div>\n",
              cssClass, node.depth * 2, node.self, errorMessage));
        }
      };
    } else {
      printer = node -> {
        char[] indents = new char[node.depth];
        Arrays.fill(indents, '\t');
        output.append(String.valueOf(indents)).append(node.self);
        if (node.hasError()) {
          output.append(" {").append(node.getError()).append('}');
        }
        output.append('\n');
      };
    }
  }

  public CharSequence run(Query query, Collection<String> ends) {
    if (isHtml) output.append("<h3>").append(query).append("</h3>\n");
    else output.append(query).append("\n");

    // Though java.util.Stream can be lazy
    // Still separate two stages to help debug
    new Searcher(includePotentialCalls, onlySearchDirectCalls, ends).search(query)
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
