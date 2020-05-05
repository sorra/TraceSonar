package sorra.tracesonar.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class BytecodeMethodParamsResolverTest {

  @Test
  public void testResolveMultiple() {
    List<String> params = new BytecodeMethodParamsResolver().resolve("([Ljava/lang/String;Z[[Ljava/lang/Byte;IJ)V");

    Assert.assertEquals(Arrays.asList("java.lang.String[]", "boolean", "java.lang.Byte[][]", "int", "long"), params);
  }

  @Test
  public void testResolveSingle() {
    List<String> params = new BytecodeMethodParamsResolver().resolve("(Ljava/lang/String;)V");

    Assert.assertEquals(Collections.singletonList("java.lang.String"), params);
  }

  @Test
  public void testResolveEmpty() {
    List<String> params = new BytecodeMethodParamsResolver().resolve("Ljava/util/List;");

    Assert.assertEquals(Collections.emptyList(), params);
  }
}
