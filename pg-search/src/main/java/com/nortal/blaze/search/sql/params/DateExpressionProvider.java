package com.nortal.blaze.search.sql.params;

import com.nortal.blaze.core.model.search.QueryParam;
import com.nortal.blaze.search.sql.SearchPrefix;
import com.nortal.blaze.util.sql.SqlBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class DateExpressionProvider extends ExpressionProvider {
  private static final Map<Integer, String> intervals;
  private static final Map<String, String> operators;

  static {
    intervals = new HashMap<>();
    intervals.put(1, "1 year");
    intervals.put(2, "1 month");
    intervals.put(3, "1 day");
    intervals.put(4, "1 hour");
    intervals.put(5, "1 minute");
    intervals.put(6, "1 second");
  }

  static {
    operators = new HashMap<>();
    operators.put(null, "&&");
    operators.put(SearchPrefix.le, "<");
    operators.put(SearchPrefix.lt, "<<");
    operators.put(SearchPrefix.ge, ">");
    operators.put(SearchPrefix.gt, ">>");
  }

  @Override
  public SqlBuilder makeExpression(QueryParam param, String alias) {
    List<SqlBuilder> ors = new ArrayList<>();
    for (String value : param.getValues()) {
      if (!StringUtils.isEmpty(value)) {
        SqlBuilder sb = new SqlBuilder("EXISTS (SELECT 1 FROM " + parasol(param, alias));
        sb.and(rangeSql("range", value)).append(")");
        ors.add(sb);
      }
    }
    return new SqlBuilder().or(ors);
  }

  public static SqlBuilder makeExpression(String field, QueryParam param) {
    List<SqlBuilder> ors = new ArrayList<>();
    for (String value : param.getValues()) {
      if (!StringUtils.isEmpty(value)) {
        ors.add(new SqlBuilder(rangeSql(field, value)));
      }
    }
    return new SqlBuilder().or(ors);
  }

  @Override
  public SqlBuilder order(String resourceType, String key, String alias) {
    String sql = String.format("SELECT range FROM " + parasol(resourceType, key, alias), alias);
    return new SqlBuilder("(" + sql + ")");
  }

  private static String rangeSql(String field, String value) {
    SearchPrefix prefix = SearchPrefix.parse(value, operators.keySet().toArray(new String[] {}));
    return field + " " + operators.get(prefix.getPrefix()) + " " + range(prefix.getValue());
  }

  private static String range(String value) {
    String[] input = StringUtils.split(value, "-T:");
    String interval = intervals.get(input.length);
    String[] mask = mask(input);
    String date = String.format("%s-%s-%sT%s:%s:%s", (Object[]) mask);
    return "range('" + date + "', '" + interval + "')";
  }

  private static String[] mask(String[] input) {
    String[] mask = new String[] { "0000", "01", "01", "00", "00", "00" };
    System.arraycopy(input, 0, mask, 0, input.length);
    return mask;
  }

}
