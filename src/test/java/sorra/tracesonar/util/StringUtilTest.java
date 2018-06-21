package sorra.tracesonar.util;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class StringUtilTest {
  @Test
  public void testSubstringBefore() {
    String sample = "a.b.Cc$Dd$Nn";

    String result = StringUtil.substringBefore(sample, "$");

    assertEquals("a.b.Cc", result);
  }

  @Test
  public void testSubstringBefore_Unchanged() {
    String sample = "a.b.Cc#Dd#Nn";

    String result = StringUtil.substringBefore(sample, "$");

    assertEquals("a.b.Cc#Dd#Nn", result);
  }

  @Test
  public void testSubstringBefore_Empty() {
    String sample = "";

    String result = StringUtil.substringBefore(sample, "$");

    assertEquals("", result);
  }

  @Test
  public void testSplitFirst() {
    String sample = "a.b.c";

    String[] result = StringUtil.splitFirst(sample, ".");

    assertArrayEquals(new String[]{"a", "b.c"}, result);
  }

  @Test
  public void testSplitFirst_Unchanged() {
    String sample = "abc";

    String[] result = StringUtil.splitFirst(sample, ".");

    assertArrayEquals(new String[]{"abc"}, result);
  }

  @Test
  public void testSplitFirst_Empty() {
    String sample = "";

    String[] result = StringUtil.splitFirst(sample, ".");

    assertArrayEquals(new String[]{""}, result);
  }

  @Test
  public void testRemoveSurrounding_Both() {
    String sample = "'abc'";

    String result = StringUtil.removeSurrounding(sample, "'");

    assertEquals("abc", result);
  }

  @Test
  public void testRemoveSurrounding_Starting() {
    String sample = "'abc";

    String result = StringUtil.removeSurrounding(sample, "'");

    assertEquals("abc", result);
  }

  @Test
  public void testRemoveSurrounding_Ending() {
    String sample = "abc'";

    String result = StringUtil.removeSurrounding(sample, "'");

    assertEquals("abc", result);
  }

  @Test
  public void testRemoveSurrounding_Unchanged() {
    String sample = "abc";

    String result = StringUtil.removeSurrounding(sample, "'");

    assertEquals("abc", result);
  }

  @Test
  public void testRemoveSurrounding_OnlyBoth() {
    String sample = "''";

    String result = StringUtil.removeSurrounding(sample, "'");

    assertEquals("", result);
  }

  @Test
  public void testRemoveSurrounding_OnlyOne() {
    String sample = "'";

    String result = StringUtil.removeSurrounding(sample, "'");

    assertEquals("", result);
  }

  @Test
  public void testRemoveSurrounding_Empty() {
    String sample = "";

    String result = StringUtil.removeSurrounding(sample, "'");

    assertEquals("", result);
  }
}
