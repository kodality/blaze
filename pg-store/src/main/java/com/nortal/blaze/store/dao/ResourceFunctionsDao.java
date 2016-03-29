package com.nortal.blaze.store.dao;

import com.nortal.blaze.util.sql.FhirJdbcTemplate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

@Component(immediate = true)
@Service(ResourceFunctionsDao.class)
public class ResourceFunctionsDao {
  @Reference
  private FhirJdbcTemplate jdbcTemplate;

  public void defineResource(String type) {
    jdbcTemplate.queryForObject("select define_resource(?)", String.class, type);
  }
}
