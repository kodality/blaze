package com.nortal.fhir.rest;

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
      String state = server.getServerInstance().isStarted() ? "STARTED" : "NOT STARTED";
      String path = server.getServerInstance().getDestination().getAddress().getAddress().getValue();
      System.out.println(name + "\t" + state + "\t" + path);
    });
    return null;
  }

}
