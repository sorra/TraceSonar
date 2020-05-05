package sorra.tracesonar.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class QueryMethodParamsResolverTest {

  @Test
  public void testResolveMultiple() {
    List<String> params = new QueryMethodParamsResolver().resolve("String[] ,boolean[][], int");

    Assert.assertEquals(Arrays.asList("String[]", "boolean[][]", "int"), params);
  }

  @Test
  public void testResolveSingle() {
    List<String> params = new QueryMethodParamsResolver().resolve("String");

    Assert.assertEquals(Collections.singletonList("String"), params);
  }


  @Test
  public void testResolveEmpty() {
    List<String> params = new QueryMethodParamsResolver().resolve("");

    Assert.assertEquals(Collections.emptyList(), params);
  }
}