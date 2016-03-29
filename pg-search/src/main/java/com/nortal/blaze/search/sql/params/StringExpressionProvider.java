package com.nortal.blaze.search.sql.params;

import com.nortal.blaze.core.model.search.QueryParam;
import com.nortal.blaze.util.sql.SqlBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class StringExpressionProvider extends ExpressionProvider {
  private static final char S = '`';// separator
  private static final Map<String, Function<String, String>> patterns;

  static {
    patterns = new HashMap<>();
    patterns.put(null, v -> S + v);// default. 'starts-with'
    patterns.put("exact", v -> S + v + S);
    patterns.put("contains", v -> v);
  }

  @Override
  public SqlBuilder makeExpression(QueryParam param, String alias) {
    String field = String.format("string(%s, %s)", alias, path(param));
    SqlBuilder sb = new SqlBuilder(field);
    return sb.append(" ~* ?", any(param.getValues(), patterns.get(param.getModifier())));
  }

  @Override
  public SqlBuilder order(String resourceType, String key, String alias) {
    return null; // TODO:
  }

  private static String any(List<String> values, Function<String, String> mapper) {
    return StringUtils.join(values.stream().map(mapper).collect(Collectors.toList()), "|");
  }

}
