package com.nortal.blaze.util.sql;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.springframework.jdbc.core.JdbcTemplate;

@Component(immediate = true, service = { FhirJdbcTemplate.class, JdbcTemplate.class })
public class FhirJdbcTemplate extends JdbcTemplate {
  @Reference(name = "dataSource")
  protected void bind(FhirDataSource ds) {
    setDataSource(ds);
  }

  protected void unbind(FhirDataSource ds) {
    // boo
  }
}
