package com.nortal.blaze.search.sql.params;

import com.nortal.blaze.core.exception.ServerException;
import com.nortal.blaze.core.model.search.QueryParam;
import com.nortal.blaze.search.dao.BlindexDao;
import com.nortal.blaze.util.sql.SqlBuilder;
import com.nortal.fhir.conformance.operations.SearchParameterMonitor;
import org.apache.commons.lang3.StringUtils;

public abstract class ExpressionProvider {

  public abstract SqlBuilder makeExpression(QueryParam param, String alias);

  public abstract SqlBuilder order(String resourceType, String key, String alias);

  protected static String path(QueryParam param) {
    return "'" + getXpath(param.getResourceType(), param.getKey()) + "'";
  }

  private static String getXpath(String resourceType, String key) {
    String xpath = SearchParameterMonitor.require(resourceType, key).getXpath();
    if (StringUtils.isEmpty(xpath)) {
      throw new ServerException("config problem. xpath empty for param " + key);
    }
    return xpath;
  }

  protected static String parasol(QueryParam param, String alias) {
    return parasol(param.getResourceType(), param.getKey(), alias);
  }

  protected static String parasol(String resourceType, String key, String alias) {
    String tblName = BlindexDao.getParasol(resourceType, getXpath(resourceType, key));
    return String.format(tblName + " WHERE resource_key = %s.key ", alias);
  }

}