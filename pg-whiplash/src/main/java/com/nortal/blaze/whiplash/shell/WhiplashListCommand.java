package com.nortal.blaze.whiplash.shell;

import com.nortal.blaze.whiplash.api.WhiplashRunner;
import org.apache.commons.lang3.StringUtils;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.osgi.framework.BundleContext;

import java.util.ArrayList;
import java.util.List;

@Command(scope = "whiplash", name = "list", description = "list propagation providing bundles")
@Service
public class WhiplashListCommand implements Action {
  @Reference
  private BundleContext bundleContext;

  @Override
  public Object execute() throws Exception {
    List<String> names = new ArrayList<>();
    bundleContext.getServiceReferences(WhiplashRunner.class,
                                       null).forEach(r -> names.add(bundleContext.getService(r).getName()));
    System.out.println(StringUtils.join(names, " "));
    return null;
  }

}
