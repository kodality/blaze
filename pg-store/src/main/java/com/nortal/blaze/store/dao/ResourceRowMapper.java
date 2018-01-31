package com.nortal.blaze.store.dao;

import com.nortal.blaze.core.model.ResourceContent;
import com.nortal.blaze.core.model.ResourceVersion;
import com.nortal.blaze.core.model.VersionId;
import com.nortal.blaze.core.util.JsonUtil;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class ResourceRowMapper implements RowMapper<ResourceVersion> {

  @Override
  public ResourceVersion mapRow(ResultSet rs, int rowNum) throws SQLException {
    ResourceVersion resource = new ResourceVersion();
    resource.setId(mapVersion(rs));
    resource.setContent(mapContent(rs));
    resource.setDeleted(rs.getString("sys_status").equals("C"));
    resource.setAuthor(JsonUtil.fromJson(rs.getString("author")));
    resource.setModified(new Date(rs.getTimestamp("last_updated").getTime()));
    return resource;
  }

  private ResourceContent mapContent(ResultSet rs) throws SQLException {
    return new ResourceContent(rs.getString("content"), "json");
  }

  private VersionId mapVersion(ResultSet rs) throws SQLException {
    return new VersionId(rs.getString("type"), rs.getString("id"), rs.getInt("last_version"));
  }

}
