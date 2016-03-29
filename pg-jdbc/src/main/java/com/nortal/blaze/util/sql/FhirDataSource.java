package com.nortal.blaze.util.sql;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

@Component(immediate = true)
@Service({ FhirDataSource.class, DataSource.class })
public class FhirDataSource extends BasicDataSource implements ManagedService {

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public void updated(Dictionary ugly) throws ConfigurationException {
    Dictionary<String, String> props = ugly;
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

  @Activate
  private void register() {
    Hashtable<String, Object> properties = new Hashtable<String, Object>();
    properties.put(Constants.SERVICE_PID, "com.nortal.blaze.pg");
    BundleContext bc = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
    bc.registerService(ManagedService.class.getName(), this, properties);
  }
}
