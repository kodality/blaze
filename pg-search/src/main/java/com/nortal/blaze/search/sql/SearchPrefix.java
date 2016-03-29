package com.nortal.blaze.search.sql;

import org.apache.commons.lang3.StringUtils;

public class SearchPrefix {
  public static final String eq = "eq";
  public static final String ne = "ne";
  public static final String gt = "gt";
  public static final String lt = "lt";
  public static final String ge = "ge";
  public static final String le = "le";
  public static final String sa = "sa";
  public static final String eb = "eb";
  public static final String ap = "ap";

  private final String prefix;
  private final String value;

  public SearchPrefix(String prefix, String value) {
    this.prefix = prefix;
    this.value = value;
  }

  public static SearchPrefix parse(String value, String... prefixes) {
    if (value == null) {
      return new SearchPrefix(null, null);
    }
    if (!StringUtils.startsWithAny(value, prefixes)) {
      return new SearchPrefix(null, value);
    }
    return new SearchPrefix(value.substring(0, 2), value.substring(2));
  }

  public String getPrefix() {
    return prefix;
  }

  public String getValue() {
    return value;
  }

}
