package com.nortal.fhir.rest.command;

import com.nortal.fhir.rest.RestResourceInitializer;
import com.nortal.fhir.rest.server.JaxRsServer;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServiceImpl;
import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import java.util.Iterator;

@Command(scope = "rest", name = "info", description = "show rest service details")
@Service
public class RestInfoCommand implements Action {
  @Argument(index = 0, description = "service name", required = false)
  private String name;
  @Reference
  private RestResourceInitializer restResourceInitializer;

  @Override
  public Object execute() throws Exception {
    if (name == null) {
      restResourceInitializer.getServers().forEach((name, server) -> printServerInfo(name, server));
      return null;
    }

    JaxRsServer server = restResourceInitializer.getServers().get(name);
    if (server == null) {
      System.out.println("service not defined for name " + name);
      return null;
    }
    printServerInfo(name, server);
    return null;
  }

  private void printServerInfo(String name, JaxRsServer server) {
    System.out.println(name);
    Server instance = server.getServerInstance();
    if (instance == null) {
      System.out.println(" instance not started");
      return;
    }
    String base = instance.getEndpoint().getEndpointInfo().getAddress();
    JAXRSServiceImpl service = (JAXRSServiceImpl) server.getServerInstance().getEndpoint().getService();
    service.getClassResourceInfos().forEach(cri -> {
      Iterator<OperationResourceInfo> i = cri.getMethodDispatcher().getOperationResourceInfos().iterator();
      i.forEachRemaining(op -> {
        String tree = i.hasNext() ? " ├── " : " └── ";
        String method = String.format("%-8s", op.getHttpMethod());
        System.out.println(tree + method + base + op.getURITemplate().getValue());
      });
    });
  }

}
