package com.nortal.blaze.util.sql;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

@Component(immediate = true)
@Service(value = { FhirJdbcTemplate.class, org.springframework.jdbc.core.JdbcTemplate.class })
@Reference(name = "dataSource", referenceInterface = FhirDataSource.class)
public class FhirJdbcTemplate extends org.springframework.jdbc.core.JdbcTemplate {
  protected void bind(FhirDataSource ds) {
    setDataSource(ds);
  }

  protected void unbind(FhirDataSource ds) {
    //
  }
}
