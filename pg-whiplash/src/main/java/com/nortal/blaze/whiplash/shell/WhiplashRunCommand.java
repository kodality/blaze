package com.nortal.blaze.whiplash.shell;

import com.nortal.blaze.whiplash.api.WhiplashRunner;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

@Command(scope = "whiplash", name = "run", description = "run blaze propagation scripts")
@Service
public class WhiplashRunCommand implements Action {
  @Argument(index = 0, description = "provider bundle name", required = true)
  private String name;
  @Option(name = "-c", aliases = "--context", description = "liquibase context", required = false, multiValued = false)
  private String context;

  @Reference
  private BundleContext bundleContext;

  @Override
  public Object execute() throws Exception {
    getRunner(name).run(context);
    return null;
  }

  private WhiplashRunner getRunner(String name) {
    Stream<WhiplashRunner> runners = getRunners().stream().filter(r -> r.getName().equals(name));
    return runners.findAny().orElseThrow(IllegalArgumentException::new);
  }

  private List<WhiplashRunner> getRunners() {
    try {
      Collection<ServiceReference<WhiplashRunner>> refs =
          bundleContext.getServiceReferences(WhiplashRunner.class, null);
      return refs.stream().map(r -> bundleContext.getService(r)).collect(Collectors.toList());
    } catch (InvalidSyntaxException e) {
      throw new RuntimeException(e);
    }
  }

}
