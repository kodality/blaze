package com.nortal.blaze.search.sql.params;

import com.nortal.blaze.core.model.search.QueryParam;
import com.nortal.blaze.util.sql.SqlBuilder;

import java.util.ArrayList;
import java.util.List;

public class TokenExpressionProvider extends ExpressionProvider {

  @Override
  public SqlBuilder makeExpression(QueryParam param, String alias) {
    List<SqlBuilder> ors = new ArrayList<>();
    for (String value : param.getValues()) {
      ors.add(token(value, param, alias));
    }
    return new SqlBuilder().or(ors);
  }

  @Override
  public SqlBuilder order(String resourceType, String key, String alias) {
    return new SqlBuilder("1"); // TODO:
  }

  private SqlBuilder token(String value, QueryParam param, String alias) {
    SqlBuilder sb = new SqlBuilder();
    sb.append(String.format("token(%s, %s)", alias, path(param)));
    sb.append(" @> array[?]::text[] ", value);
    return sb;
  }

}
