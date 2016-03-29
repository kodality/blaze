package com.nortal.blaze.store.dao;

import com.nortal.blaze.core.model.ResourceId;
import com.nortal.blaze.core.model.ResourceVersion;
import com.nortal.blaze.core.model.VersionId;
import com.nortal.blaze.util.sql.FhirJdbcTemplate;
import com.nortal.blaze.util.sql.SqlBuilder;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

@Component(immediate = true)
@Service(ResourceDao.class)
public class ResourceDao {
  @Reference
  private FhirJdbcTemplate jdbcTemplate;

  public void create(ResourceVersion version) {
    String key = jdbcTemplate.queryForObject("select nextval('resource_key_seq')", String.class);
    if (version.getId().getResourceId() == null) {
      version.getId().setResourceId(key);
    }
    String sql = "INSERT INTO resource (key, type, id, last_version, content) VALUES (?,?,?,?,CAST(? as jsonb))";
    jdbcTemplate.update(sql,
                        key,
                        version.getId().getResourceType(),
                        version.getId().getResourceId(),
                        version.getId().getVersion(),
                        version.getContent().getValue());
  }

  public Integer getLastVersion(ResourceId id) {
    String sql = "SELECT COALESCE(max(last_version),0) FROM resource WHERE type = ? AND id = ?";
    return jdbcTemplate.queryForObject(sql, Integer.class, id.getResourceType(), id.getResourceId());
  }

  public ResourceVersion load(VersionId id) {
    SqlBuilder sb = new SqlBuilder();
    sb.append("SELECT * FROM resource WHERE type = ? AND id = ?", id.getResourceType(), id.getResourceId());
    if (id.getVersion() != null) {
      sb.append(" AND last_version = ?", id.getVersion());
    } else {
      sb.append(" AND sys_status = 'A'");
    }
    try {
      return jdbcTemplate.queryForObject(sb.getSql(), new ResourceRowMapper(), sb.getParams());
    } catch (IncorrectResultSizeDataAccessException e) {
      return null;
    }
  }
}
