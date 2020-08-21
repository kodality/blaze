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
 package com.kodality.blaze.search.sql.params;

import com.kodality.blaze.core.exception.FhirServerException;
import com.kodality.blaze.core.model.search.QueryParam;
import com.kodality.blaze.core.service.conformance.ConformanceHolder;
import com.kodality.blaze.search.dao.BlindexDao;
import com.kodality.blaze.search.util.FhirPathHackUtil;
import com.kodality.blaze.util.sql.SqlBuilder;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.Stream;

public abstract class ExpressionProvider {

  public abstract SqlBuilder makeExpression(QueryParam param, String alias);

  public abstract SqlBuilder order(String resourceType, String key, String alias);

  protected static String path(QueryParam param) {
    return path(param.getResourceType(), param.getKey());
  }

  protected static String path(String resourceType, String key) {
    return "'" + getPath(resourceType, key) + "'";
  }

  private static String getPath(String resourceType, String key) {
    String expr = ConformanceHolder.requireSearchParam(resourceType, key).getExpression();
    String path = Stream.of(expr.split("\\|"))
        .map((s) -> StringUtils.trim(s))
        .filter(e -> e.startsWith(resourceType))
        .findFirst()
        .orElse(null);
    if (StringUtils.isEmpty(path)) {
      throw new FhirServerException(500, "config problem. path empty for param " + key);
    }
    path = FhirPathHackUtil.replaceAs(path);
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
