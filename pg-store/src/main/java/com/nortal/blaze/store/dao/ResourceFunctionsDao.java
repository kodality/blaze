package com.nortal.blaze.store.dao;

import com.nortal.blaze.util.sql.FhirJdbcTemplate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(immediate = true, service = ResourceFunctionsDao.class)
public class ResourceFunctionsDao {
  @Reference
  private FhirJdbcTemplate jdbcTemplate;

  public void defineResource(String type) {
    jdbcTemplate.queryForObject("select define_resource(?)", String.class, type);
  }
}
