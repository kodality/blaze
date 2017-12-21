package com.nortal.blaze.util.sql;

import com.google.gson.Gson;
import com.nortal.blaze.auth.ClientIdentity;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Component(immediate = true, service = { FhirDataSource.class, DataSource.class }, configurationPid = "com.nortal.blaze.pg")
public class FhirDataSource extends SimpleDataSource {
  @Reference
  private ClientIdentity clientIdentity;

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
