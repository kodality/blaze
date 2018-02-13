package com.nortal.blaze.search.sql.params;

import com.nortal.blaze.core.model.search.QueryParam;
import com.nortal.blaze.core.service.conformance.ConformanceHolder;
import com.nortal.blaze.search.sql.SqlToster;
import com.nortal.blaze.util.sql.SqlBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.hl7.fhir.dstu3.model.CodeType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class ReferenceExpressionProvider extends ExpressionProvider {
  private static ThreadLocalInteger I = new ThreadLocalInteger();

  @Override
  public SqlBuilder makeExpression(QueryParam param, String alias) {
    Validate.isTrue(param.getChain() == null);
    List<SqlBuilder> ors = param.getValues().stream().map(v -> reference(v, param, alias)).collect(toList());
    return new SqlBuilder().or(ors);
  }

  @Override
  public SqlBuilder order(String resourceType, String key, String alias) {
    return null; // TODO:
  }

  private static SqlBuilder reference(String value, QueryParam param, String alias) {
    SqlBuilder sb = new SqlBuilder();
    sb.append(String.format("reference(%s, %s)", alias, path(param)));
    if (StringUtils.isEmpty(value)) {
      return sb.append(" = array[null]");
    }
    return sb.append(" @> array[?]::text[] ", value);
  }

  public static SqlBuilder chain(List<QueryParam> params, String parentAlias) {
    I.remove();
    SqlBuilder sb = new SqlBuilder();
    for (QueryParam param : params) {
      sb.append(chain(param, parentAlias));
    }
    return sb;
  }

  private static SqlBuilder chain(QueryParam param, String parentAlias) {
    SqlBuilder sb = new SqlBuilder();
    if (param.getChain() == null) {
      sb.and("(").append(SqlToster.condition(param, parentAlias)).append(")");
      return sb;
    }
    String[] refs = getReferencedTypes(param);
    String alias = generateAlias(refs.length == 1 ? refs[0].toLowerCase() : "friends");
    sb.append("INNER JOIN resource " + alias);
    sb.append(" ON ").in(alias + ".type", (Object[]) refs);
    sb.and(String.format("reference(%s, %s) && ref(%s.type, %s.id)", parentAlias, path(param), alias, alias));
    sb.append(chain(param.getChain(), alias));
    return sb;
  }

  private static String generateAlias(String key) {
    Integer i = I.get(key);
    return key + (i == null ? "" : i);
  }

  private static String[] getReferencedTypes(QueryParam param) {
    if (param.getModifier() != null) {
      return new String[] { param.getModifier() };
    }
    return getParamTargets(param).toArray(new String[] {});
  }

  private static Set<String> getParamTargets(QueryParam param) {
    List<CodeType> targets = ConformanceHolder.requireSearchParam(param.getResourceType(), param.getKey()).getTarget();
    return targets.stream().map(c -> c.getValue()).collect(Collectors.toSet());
  }

  // private String fixReference(String input, QueryParam param) {
  // if(StringUtils.countMatches(input, "/") == 1){
  // return input;
  // }
  // SearchParameter confSearchParameter = SearchParameterMonitor.get(param.getResourceType(), param.getKey());
  // confSearchParameter.getTarget()
  // }

  private static class ThreadLocalInteger extends ThreadLocal<Map<String, Integer>> {
    @Override
    protected Map<String, Integer> initialValue() {
      return new HashMap<String, Integer>();
    }

    public Integer get(String key) {
      Integer i = get().getOrDefault(key, 0);
      return get().put(key, ++i);
    }
  }

}
