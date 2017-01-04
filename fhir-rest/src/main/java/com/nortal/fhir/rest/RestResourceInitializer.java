package com.nortal.fhir.rest;

import com.nortal.blaze.core.util.Osgi;
import com.nortal.fhir.conformance.operations.CapabilityStatementListener;
import com.nortal.fhir.conformance.operations.CapabilityStatementMonitor;
import com.nortal.fhir.rest.server.FhirResourceServer;
import com.nortal.fhir.rest.server.FhirResourceServerFactory;
import com.nortal.fhir.rest.server.FhirRootServer;
import com.nortal.fhir.rest.server.JaxRsServer;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.common.classloader.ClassLoaderUtils;
import org.apache.cxf.endpoint.Server;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Service;
import org.hl7.fhir.dstu3.model.CapabilityStatement;
import org.hl7.fhir.dstu3.model.CapabilityStatement.CapabilityStatementRestComponent;
import org.hl7.fhir.dstu3.model.CapabilityStatement.CapabilityStatementRestResourceComponent;
import org.hl7.fhir.dstu3.model.CapabilityStatement.RestfulCapabilityMode;

@Component(immediate = true)
@Service(value = CapabilityStatementListener.class)
public class RestResourceInitializer implements CapabilityStatementListener {
  private final Map<String, Server> servers = new HashMap<>();

  @Override
  public void comply(CapabilityStatement capabilityStatement) {
    new Thread(() -> {
      // hack cxf when trying to load by class name from wrong classloader
      ClassLoaderUtils.setThreadContextClassloader(RestResourceInitializer.class.getClassLoader());
      realComply(capabilityStatement);
    }).run();
  }

  private void realComply(CapabilityStatement capabilityStatement) {
    stop();
    if (capabilityStatement == null) {
      return;
    }
    for (CapabilityStatementRestComponent rest : capabilityStatement.getRest()) {
      if (rest.getMode() == RestfulCapabilityMode.SERVER) {
        start(rest);
      }
    }
  }

  @Activate
  private void start() {
    comply(CapabilityStatementMonitor.getCapabilityStatement());
  }

  @Deactivate
  private void stop() {
    servers.values().forEach(s -> s.destroy());
    servers.clear();
  }

  private void start(CapabilityStatementRestComponent rest) {
    start(null, new FhirRootServer(rest));
    rest.getResource().forEach(rr -> start(rr));
  }

  private void start(CapabilityStatementRestResourceComponent resourceRest) {
    String type = resourceRest.getType();
    for (FhirResourceServerFactory factory : Osgi.getBeans(FhirResourceServerFactory.class)) {
      if (StringUtils.equals(factory.getType(), type)) {
        start(type, factory.construct(resourceRest));
        return;
      }
    }
    start(type, new FhirResourceServer(resourceRest));
  }

  private void start(String type, JaxRsServer server) {
    servers.put(type, server.createServer());
  }

  public Map<String, Server> getServers() {
    return servers;
  }

}
