package com.nortal.blaze.search;

import com.nortal.blaze.util.sql.SimpleDataSource;
import com.nortal.blaze.whiplash.api.WhiplashRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.sql.DataSource;

@Component(immediate = true, service = WhiplashRunner.class)
public class SearchWhiplashRunner extends WhiplashRunner {
  @Reference
  private SimpleDataSource dataSource;

  public SearchWhiplashRunner() {
    super("pg-search/changeset.xml");
  }

  @Override
  protected BundleContext getBundleContext() {
    return FrameworkUtil.getBundle(SearchWhiplashRunner.class).getBundleContext();
  }

  @Override
  protected DataSource getDataSource() {
    return dataSource;
  }

}