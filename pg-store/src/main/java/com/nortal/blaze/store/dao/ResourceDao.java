package com.nortal.blaze.store.dao;

import com.google.gson.Gson;
import com.nortal.blaze.core.model.ResourceId;
import com.nortal.blaze.core.model.ResourceVersion;
import com.nortal.blaze.core.model.VersionId;
import com.nortal.blaze.util.sql.FhirJdbcTemplate;
import com.nortal.blaze.util.sql.SqlBuilder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import java.util.List;

@Component(immediate = true, service = ResourceDao.class)
public class ResourceDao {
  @Reference
  private FhirJdbcTemplate jdbcTemplate;

  public void create(ResourceVersion version) {
    Long key = jdbcTemplate.queryForObject("select nextval('resource_key_seq')", Long.class);
    if (version.getId().getResourceId() == null) {
      version.getId().setResourceId(key.toString());
    }
    String sql =
        "INSERT INTO resource (key, type, id, last_version, author, content, sys_status) VALUES (?,?,?,?,?::jsonb,?::jsonb,?)";
    jdbcTemplate.update(sql,
                        key,
                        version.getId().getResourceType(),
                        version.getId().getResourceId(),
                        version.getId().getVersion(),
                        new Gson().toJson(version.getAuthor()),
                        version.getContent() == null ? null : version.getContent().getValue(),
                        version.isDeleted() ? "C" : "A");
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
      sb.append(" AND sys_status != 'T'");
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

  public List<ResourceVersion> loadHistory(ResourceId id) {
    SqlBuilder sb = new SqlBuilder();
    sb.append("SELECT * FROM resource WHERE type = ? AND id = ?", id.getResourceType(), id.getResourceId());
    sb.append(" ORDER BY last_version desc");
    return jdbcTemplate.query(sb.getSql(), new ResourceRowMapper(), sb.getParams());
  }
}
