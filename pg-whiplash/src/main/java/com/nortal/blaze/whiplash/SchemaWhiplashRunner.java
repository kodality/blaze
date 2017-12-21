package com.nortal.blaze.whiplash;

import com.nortal.blaze.util.sql.SimpleDataSource;
import com.nortal.blaze.whiplash.api.WhiplashRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.sql.DataSource;

@Component(immediate = true, service = WhiplashRunner.class)
public class SchemaWhiplashRunner extends WhiplashRunner {
  @Reference
  private SimpleDataSource dataSource;

  public SchemaWhiplashRunner() {
    super("pg-whiplash/core/changelog.xml");
  }

  @Override
  public String getName() {
    return "init-db";
  }

  @Override
  protected BundleContext getBundleContext() {
    return FrameworkUtil.getBundle(SchemaWhiplashRunner.class).getBundleContext();
  }

  @Override
  protected DataSource getDataSource() {
    return dataSource;
  }

}
