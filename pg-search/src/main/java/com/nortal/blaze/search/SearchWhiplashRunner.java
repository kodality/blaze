package com.nortal.blaze.search;

import com.nortal.blaze.whiplash.api.WhiplashRunner;
import javax.sql.DataSource;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

@Component(immediate = true)
@Service(WhiplashRunner.class)
public class SearchWhiplashRunner extends WhiplashRunner {
  @Reference
  private DataSource dataSource;

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