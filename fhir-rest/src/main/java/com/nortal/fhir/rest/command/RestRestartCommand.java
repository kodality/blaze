package com.nortal.fhir.rest.command;

import com.nortal.fhir.rest.RestResourceInitializer;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

@Command(scope = "rest", name = "restart", description = "restart all rest services")
@Service
public class RestRestartCommand implements Action {
  @Reference
  private RestResourceInitializer restResourceInitializer;

  @Override
  public Object execute() throws Exception {
    restResourceInitializer.restart();
    return null;
  }

}
