package com.nortal.blaze.search.sql;

import org.junit.Assert;
import org.junit.Test;

public class SearchPrefixTest {
  private static final String[] prefixes = new String[] { SearchPrefix.eq, SearchPrefix.le };

  @Test
  public void test() {
    test(null, null, null);
    test("", null, "");
    test("2", null, "2");
    test("eq2", "eq", "2");
    test("le2", "le", "2");
    test("le", "le", "");
    test("eqle2", "eq", "le2");
  }

  private void test(String value, String expectedPrefix, String expectedValue) {
    SearchPrefix result = SearchPrefix.parse(value, prefixes);
    Assert.assertEquals(result.getPrefix(), expectedPrefix);
    Assert.assertEquals(result.getValue(), expectedValue);
  }
}
