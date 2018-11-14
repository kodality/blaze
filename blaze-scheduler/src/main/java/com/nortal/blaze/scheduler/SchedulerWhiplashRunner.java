package com.nortal.blaze.scheduler;

import com.nortal.blaze.util.sql.SimpleDataSource;
import com.nortal.blaze.whiplash.api.WhiplashRunner;
import liquibase.exception.LiquibaseException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.sql.DataSource;

import java.sql.SQLException;

@Component(immediate = true, service = WhiplashRunner.class)
public class SchedulerWhiplashRunner extends WhiplashRunner {
  @Reference
  private SimpleDataSource dataSource;

  public SchedulerWhiplashRunner() {
    super("pg-blaze-scheduler/changeset.xml");
  }
  
  @Activate
  public void init() throws SQLException, LiquibaseException {
    run();
  }

  @Override
  protected BundleContext getBundleContext() {
    return FrameworkUtil.getBundle(SchedulerWhiplashRunner.class).getBundleContext();
  }

  @Override
  protected DataSource getDataSource() {
    return dataSource;
  }

}
