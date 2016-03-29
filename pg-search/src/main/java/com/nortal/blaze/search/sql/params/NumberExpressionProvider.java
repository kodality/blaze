package com.nortal.blaze.search.sql.params;

import com.nortal.blaze.core.model.search.QueryParam;
import com.nortal.blaze.search.sql.SearchPrefix;
import com.nortal.blaze.util.sql.SqlBuilder;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class NumberExpressionProvider extends ExpressionProvider {
  private static final Map<Integer, BigDecimal> precisions = new HashMap<Integer, BigDecimal>() {
    @Override
    public BigDecimal get(Object key) {
      return computeIfAbsent((Integer) key, k -> new BigDecimal("0." + StringUtils.repeat('0', k) + "5"));
    };
  };
  private static final Map<String, String> operators;

  static {
    operators = new HashMap<>();
    operators.put(null, "=");
    operators.put(SearchPrefix.le, "<=");
    operators.put(SearchPrefix.lt, "<");
    operators.put(SearchPrefix.ge, ">=");
    operators.put(SearchPrefix.gt, ">");
    operators.put(SearchPrefix.ne, "<>");
  }

  @Override
  public SqlBuilder makeExpression(QueryParam param, String alias) {
    List<SqlBuilder> ors = new ArrayList<>();
    for (String value : param.getValues()) {
      if (!StringUtils.isEmpty(value)) {
        ors.add(date(value, param, alias));
      }
    }
    return new SqlBuilder().or(ors);
  }

  @Override
  public SqlBuilder order(String resourceType, String key, String alias) {
    return null; // TODO:
  }

  private SqlBuilder date(String value, QueryParam param, String alias) {
    SqlBuilder sb = new SqlBuilder();
    SearchPrefix prefix = SearchPrefix.parse(value, operators.keySet().toArray(new String[] {}));
    String op = operators.get(prefix.getPrefix());
    BigDecimal number = new BigDecimal(prefix.getValue());

    sb.append("EXISTS (SELECT 1 FROM " + parasol(param, alias));
    if (prefix.getPrefix() == null) {
      sb.and("number BETWEEN ? AND ?", lower(number), upper(number));
    } else {
      sb.and("number " + op + "?", number);
    }
    sb.append(")");
    return sb;
  }

  private static BigDecimal lower(BigDecimal number) {
    return number.subtract(precisions.get(number.scale()));
  }

  private static BigDecimal upper(BigDecimal number) {
    return number.add(precisions.get(number.scale()));
  }

}
