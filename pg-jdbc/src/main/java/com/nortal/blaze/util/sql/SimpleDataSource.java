package com.nortal.blaze.util.sql;

import org.apache.commons.dbcp2.BasicDataSource;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

@Component(immediate = true, service = { SimpleDataSource.class }, configurationPid = "com.nortal.blaze.pg")
public class SimpleDataSource extends BasicDataSource {

  @Activate
  void activate(Map<String, String> props) {
    updated(props);
  }

  @Modified
  public void updated(Map<String, String> props) {
    setDriverClassLoader(this.getClass().getClassLoader());
    setDriverClassName("org.postgresql.Driver");
    setInitialSize(5);
    setValidationQuery("select 1");
    setConnectionInitSqls(Collections.singleton("set search_path to fhir,core,public"));

    setMaxTotal(Integer.valueOf(props.get("db.maxActive")));
    setUrl(props.get("db.url"));
    setUsername(props.get("db.username"));
    setPassword(props.get("db.password"));

    try {
      createDataSource();
    } catch (SQLException e) {
      System.out.println("woo");
    }
  }
  
}
