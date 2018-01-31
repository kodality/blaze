package com.nortal.fhir.rest.command;

import com.nortal.fhir.rest.RestResourceInitializer;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

@Command(scope = "rest", name = "list", description = "show started rest services")
@Service
public class RestListCommand implements Action {
  @Reference
  private RestResourceInitializer restResourceInitializer;

  @Override
  public Object execute() throws Exception {
    restResourceInitializer.getServers().forEach((name, server) -> {
      String state = server.getServerInstance().isStarted() ? "STARTED" : "unknown";
      String path = server.getServerInstance().getDestination().getAddress().getAddress().getValue();
      System.out.println(String.format("%-26s", name) + " " + state + " " + path);
    });
    return null;
  }

}
