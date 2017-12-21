package com.nortal.blaze.store;

import com.nortal.blaze.util.sql.SimpleDataSource;
import com.nortal.blaze.whiplash.api.WhiplashRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.sql.DataSource;

@Component(immediate = true, service = WhiplashRunner.class)
public class StoreWhiplashRunner extends WhiplashRunner {
  @Reference
  private SimpleDataSource dataSource;

  public StoreWhiplashRunner() {
    super("pg-store/changeset.xml");
  }

  @Override
  protected BundleContext getBundleContext() {
    return FrameworkUtil.getBundle(StoreWhiplashRunner.class).getBundleContext();
  }

  @Override
  protected DataSource getDataSource() {
    return dataSource;
  }

}
