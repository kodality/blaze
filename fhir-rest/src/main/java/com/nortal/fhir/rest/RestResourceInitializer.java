package com.nortal.fhir.rest;

import com.nortal.blaze.core.util.Osgi;
import com.nortal.fhir.conformance.operations.ConformanceListener;
import com.nortal.fhir.conformance.operations.ConformanceMonitor;
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
import org.hl7.fhir.instance.model.Conformance;
import org.hl7.fhir.instance.model.Conformance.ConformanceRestComponent;
import org.hl7.fhir.instance.model.Conformance.ConformanceRestResourceComponent;
import org.hl7.fhir.instance.model.Conformance.RestfulConformanceMode;

@Component(immediate = true)
@Service(value = ConformanceListener.class)
public class RestResourceInitializer implements ConformanceListener {
  private final Map<String, Server> servers = new HashMap<>();

  @Override
  public void comply(Conformance conformance) {
    new Thread(new Runnable() {

      @Override
      public void run() {
        // hack cxf when trying to load by class name from wrong classloader
        ClassLoaderUtils.setThreadContextClassloader(RestResourceInitializer.class.getClassLoader());
        realComply(conformance);
      }
    }).run();
  }

  private void realComply(Conformance conformance) {
    stop();
    if (conformance == null) {
      return;
    }
    for (ConformanceRestComponent rest : conformance.getRest()) {
      if (rest.getMode() == RestfulConformanceMode.SERVER) {
        start(rest);
      }
    }
  }

  @Activate
  private void start() {
    comply(ConformanceMonitor.getConformance());
  }

  @Deactivate
  private void stop() {
    servers.values().forEach(s -> s.destroy());
    servers.clear();
  }

  private void start(ConformanceRestComponent rest) {
    start(null, new FhirRootServer(rest));
    rest.getResource().forEach(rr -> start(rr));
  }

  private void start(ConformanceRestResourceComponent resourceRest) {
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
