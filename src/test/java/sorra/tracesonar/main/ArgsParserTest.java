package sorra.tracesonar.main;

import java.util.Arrays;

import org.junit.Test;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static sorra.tracesonar.main.ArgsParser.Option.*;

public class ArgsParserTest {
  @Test
  public void testAllOptions() {
    String[] args = {
        "-f", "file1", "file2",
        "-q", "query1", "query2",
        "-p",
        "--exclude",
        "--include-only", "io1",
        "--stop-at", "sa1", "sa2", "sa3"};

    ArgsParser parser = new ArgsParser(args);

    assertEquals(Arrays.asList("file1", "file2"), parser.getOptionValues(FILE));
    assertEquals(Arrays.asList("query1", "query2"), parser.getOptionValues(QUERY));
    assertEquals(singletonList("true"), parser.getOptionValues(POTENTIAL));
    assertEquals(emptyList(), parser.getOptionValues(EXCLUDE));
    assertEquals(singletonList("io1"), parser.getOptionValues(INCLUDE_ONLY));
    assertEquals(Arrays.asList("sa1", "sa2", "sa3"), parser.getOptionValues(STOP_AT));
  }
}
