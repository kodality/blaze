package com.nortal.blaze.util.sql;

import com.google.gson.Gson;
import com.nortal.blaze.auth.ClientIdentity;
import org.apache.commons.dbcp2.BasicDataSource;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

@Component(immediate = true, service = { FhirDataSource.class,
                                         DataSource.class }, configurationPid = "com.nortal.blaze.pg")
public class FhirDataSource extends BasicDataSource {
  @Reference
  private ClientIdentity clientIdentity;

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
    setConnectionInitSqls(Collections.singleton("set search_path to fhir,public"));

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

  @Override
  public Connection getConnection() throws SQLException {
    Connection c = super.getConnection();
    String sql = "SELECT core.set_user(?::jsonb)";
    try (PreparedStatement ps = c.prepareStatement(sql)) {
      String json = new Gson().toJson(clientIdentity.get());
      ps.setString(1, json);
      ps.execute();
    }
    return c;
  }

}
