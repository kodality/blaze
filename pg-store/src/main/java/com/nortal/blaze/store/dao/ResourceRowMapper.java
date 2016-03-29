package com.nortal.blaze.store.dao;

import com.nortal.blaze.core.model.ResourceContent;
import com.nortal.blaze.core.model.ResourceVersion;
import com.nortal.blaze.core.model.VersionId;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

public class ResourceRowMapper implements RowMapper<ResourceVersion> {

  @Override
  public ResourceVersion mapRow(ResultSet rs, int rowNum) throws SQLException {
    ResourceVersion resource = new ResourceVersion();
    resource.setId(mapVersion(rs));
    resource.setContent(mapContent(rs));
    return resource;
  }

  private ResourceContent mapContent(ResultSet rs) throws SQLException {
    return new ResourceContent(rs.getString("content"), "json");
  }

  private VersionId mapVersion(ResultSet rs) throws SQLException {
    return new VersionId(rs.getString("type"), rs.getString("id"), rs.getInt("last_version"));
  }

}
