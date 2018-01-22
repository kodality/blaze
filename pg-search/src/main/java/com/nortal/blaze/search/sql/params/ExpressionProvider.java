package com.nortal.blaze.search.sql.params;

import com.nortal.blaze.core.exception.ServerException;
import com.nortal.blaze.core.model.search.QueryParam;
import com.nortal.blaze.search.dao.BlindexDao;
import com.nortal.blaze.util.sql.SqlBuilder;
import com.nortal.fhir.conformance.operations.SearchParameterMonitor;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.Stream;

public abstract class ExpressionProvider {

  public abstract SqlBuilder makeExpression(QueryParam param, String alias);

  public abstract SqlBuilder order(String resourceType, String key, String alias);

  protected static String path(QueryParam param) {
    return "'" + getPath(param.getResourceType(), param.getKey()) + "'";
  }

  private static String getPath(String resourceType, String key) {
    String expr = SearchParameterMonitor.require(resourceType, key).getExpression();
    String path =
        Stream.of(expr.split("\\|")).map((s) -> StringUtils.trim(s)).filter(e -> e.startsWith(resourceType)).findFirst().orElse(null);
    if (StringUtils.isEmpty(path)) {
      throw new ServerException("config problem. path empty for param " + key);
    }
    return StringUtils.removeFirst(path, resourceType + "\\.");
  }

  protected static String parasol(QueryParam param, String alias) {
    return parasol(param.getResourceType(), param.getKey(), alias);
  }

  protected static String parasol(String resourceType, String key, String alias) {
    String tblName = BlindexDao.getParasol(resourceType, getPath(resourceType, key));
    return String.format(tblName + " WHERE resource_key = %s.key ", alias);
  }

}
