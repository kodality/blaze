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
package com.kodality.blaze.store.dao;

import com.kodality.blaze.core.model.ResourceId;
import com.kodality.blaze.core.model.ResourceVersion;
import com.kodality.blaze.core.model.VersionId;
import com.kodality.blaze.core.model.search.HistorySearchCriterion;
import com.kodality.blaze.core.util.DateUtil;
import com.kodality.blaze.core.util.JsonUtil;
import com.kodality.blaze.store.api.PgResourceFilter;
import com.kodality.blaze.util.sql.FhirJdbcTemplate;
import com.kodality.blaze.util.sql.SqlBuilder;
import java.util.List;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import static org.osgi.service.component.annotations.ReferencePolicy.DYNAMIC;

@Component(immediate = true, service = ResourceDao.class)
public class ResourceDao {
  @Reference
  private FhirJdbcTemplate jdbcTemplate;

  private PgResourceFilter pgResourceFilter;

  public String getNextResourceId() {
    //TODO: may already exist
    return String.valueOf(jdbcTemplate.queryForObject("select nextval('resource_id_seq')", Long.class));
  }

  public void create(ResourceVersion version) {
    if (version.getId().getResourceId() == null) {
      version.getId().setResourceId(getNextResourceId());
    }
    String sql =
        "INSERT INTO resource (type, id, last_version, author, content, sys_status) VALUES (?,?,?,?::jsonb,?::jsonb,?)";
    jdbcTemplate.update(sql,
        version.getId().getResourceType(),
        version.getId().getResourceId(),
        version.getId().getVersion(),
        JsonUtil.toJson(version.getAuthor()),
        version.getContent() == null ? null : version.getContent().getValue(),
        version.isDeleted() ? "C" : "A");
  }

  public Integer getLastVersion(ResourceId id) {
    String sql = "SELECT COALESCE(max(last_version),0) FROM resource WHERE type = ? AND id = ?";
    return jdbcTemplate.queryForObject(sql, Integer.class, id.getResourceType(), id.getResourceId());
  }

  public ResourceVersion load(VersionId id) {
    SqlBuilder sb = new SqlBuilder();
    sb.append("SELECT * FROM resource r WHERE type = ? AND id = ?", id.getResourceType(), id.getResourceId());
    if (id.getVersion() != null) {
      sb.append(" AND last_version = ?", id.getVersion());
    } else {
      sb.append(" AND sys_status != 'T'");
    }
    if (pgResourceFilter != null) {
      pgResourceFilter.filter(sb, "r");
    }
    try {
      return jdbcTemplate.queryForObject(sb.getSql(), new ResourceRowMapper(), sb.getParams());
    } catch (IncorrectResultSizeDataAccessException e) {
      if (e.getActualSize() == 0) {
        return null;
      }
      throw e;
    }
  }

  public List<ResourceVersion> loadHistory(HistorySearchCriterion criteria) {
    SqlBuilder sb = new SqlBuilder();
    sb.append("SELECT * FROM resource r WHERE 1=1");
    sb.appendIfNotNull(" AND type = ?", criteria.getResourceType());
    sb.appendIfNotNull(" AND id = ?", criteria.getResourceId());
    if (criteria.getSince() != null) {
      sb.append(" AND last_updated >= ?", DateUtil.parse(criteria.getSince()));
    }
    if (pgResourceFilter != null) {
      pgResourceFilter.filter(sb, "r");
    }
    sb.append(" ORDER BY last_updated desc");
    return jdbcTemplate.query(sb.getSql(), new ResourceRowMapper(), sb.getParams());
  }

  @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = DYNAMIC, service = PgResourceFilter.class, name = "PgResourceFilter")
  protected void bind(PgResourceFilter pgResourceFilter) {
    this.pgResourceFilter = pgResourceFilter;
  }

  protected void unbind(PgResourceFilter pgResourceFilter) {
    this.pgResourceFilter = null;
  }

}
