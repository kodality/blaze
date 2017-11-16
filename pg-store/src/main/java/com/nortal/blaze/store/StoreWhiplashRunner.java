package com.nortal.blaze.store;

import com.nortal.blaze.whiplash.api.WhiplashRunner;
import javax.sql.DataSource;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(immediate = true, service = WhiplashRunner.class)
public class StoreWhiplashRunner extends WhiplashRunner {
  @Reference
  private DataSource dataSource;

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
