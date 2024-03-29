/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 package com.kodality.blaze.search.sql;

import com.kodality.blaze.core.exception.FhirException;
import com.kodality.blaze.core.model.search.QueryParam;
import com.kodality.blaze.core.service.conformance.ConformanceHolder;
import com.kodality.blaze.search.sql.params.DateExpressionProvider;
import com.kodality.blaze.search.sql.params.ExpressionProvider;
import com.kodality.blaze.search.sql.params.NumberExpressionProvider;
import com.kodality.blaze.search.sql.params.ReferenceExpressionProvider;
import com.kodality.blaze.search.sql.params.StringExpressionProvider;
import com.kodality.blaze.search.sql.params.TokenExpressionProvider;
import com.kodality.blaze.search.sql.params.*;
import com.kodality.blaze.util.sql.SqlBuilder;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SqlToster {
  private static final Map<String, SpecialParamBuilder> specialParams;
  private static final Map<SearchParamType, ExpressionProvider> providers;

  static {
    specialParams = new HashMap<>();
    specialParams.put("_id", (p, a) -> new SqlBuilder().in(a + ".id", p.getValues()));
    specialParams.put("_lastUpdated",
                      (p, a) -> DateExpressionProvider.makeExpression("range_instant(" + a + ".last_updated)", p));
    specialParams.put("_content", (p, a) -> {
      throw new FhirException(400, IssueType.NOTSUPPORTED, "_content not implemented");
    });
    specialParams.put("_text", (p, a) -> {
      throw new FhirException(400, IssueType.NOTSUPPORTED, "_text not implemented");
    });

    providers = new HashMap<SearchParamType, ExpressionProvider>();
    providers.put(SearchParamType.STRING, new StringExpressionProvider());
    providers.put(SearchParamType.TOKEN, new TokenExpressionProvider());
    providers.put(SearchParamType.DATE, new DateExpressionProvider());
    providers.put(SearchParamType.REFERENCE, new ReferenceExpressionProvider());
    providers.put(SearchParamType.NUMBER, new NumberExpressionProvider());
  }

  private SqlToster() {
    //
  }

  public static SqlBuilder chain(List<QueryParam> params, String alias) {
    return ReferenceExpressionProvider.chain(params, alias);
  }

  public static SqlBuilder condition(QueryParam param, String alias) {
    String key = param.getKey();
    if (specialParams.containsKey(key)) {
      return specialParams.get(key).build(param, alias);
    }
    if (!providers.containsKey(param.getType())) {
      String details = "'" + param.getType() + "' search parameter type not implemented";
      throw new FhirException(400, IssueType.NOTSUPPORTED, details);
    }
    return providers.get(param.getType()).makeExpression(param, alias);
  }

  public static SqlBuilder order(QueryParam param, String alias) {
    String value = param.getValues().get(0);
    String direction = "ASC";
    if (value.startsWith("-") || "desc".equals(param.getModifier())) {
      direction = "DESC";
      value = value.replaceFirst("-", "");
    }
    SearchParamType type = ConformanceHolder.requireSearchParam(param.getResourceType(), value).getType();
    // String key = param.getKey();
    // if (specialParams.containsKey(key)) {
    // return specialParams.get(key).build(param, alias);
    // }
    if (!providers.containsKey(type)) {
      String details = String.format("'%s' search parameter type not implemented", param.getType());
      throw new FhirException(400, IssueType.NOTSUPPORTED, details);
    }
    return providers.get(type).order(param.getResourceType(), value, alias).append(direction);
  }

  private interface SpecialParamBuilder {
    SqlBuilder build(QueryParam param, String alias);
  }
}
