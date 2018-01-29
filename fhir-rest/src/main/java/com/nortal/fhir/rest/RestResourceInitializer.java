package com.nortal.fhir.rest;

import com.nortal.blaze.core.util.Osgi;
import com.nortal.fhir.conformance.capability.CapabilityStatementListener;
import com.nortal.fhir.conformance.capability.CapabilityStatementMonitor;
import com.nortal.fhir.conformance.definition.ResourceDefinitionListener;
import com.nortal.fhir.conformance.definition.ResourceDefinitionsMonitor;
import com.nortal.fhir.rest.server.FhirResourceServer;
import com.nortal.fhir.rest.server.FhirResourceServerFactory;
import com.nortal.fhir.rest.server.FhirRootServer;
import com.nortal.fhir.rest.server.JaxRsServer;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.common.classloader.ClassLoaderUtils;
import org.hl7.fhir.dstu3.model.CapabilityStatement;
import org.hl7.fhir.dstu3.model.CapabilityStatement.CapabilityStatementRestComponent;
import org.hl7.fhir.dstu3.model.CapabilityStatement.CapabilityStatementRestResourceComponent;
import org.hl7.fhir.dstu3.model.CapabilityStatement.ConditionalDeleteStatus;
import org.hl7.fhir.dstu3.model.CapabilityStatement.RestfulCapabilityMode;
import org.hl7.fhir.dstu3.model.CapabilityStatement.UnknownContentCode;
import org.hl7.fhir.dstu3.model.StructureDefinition;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Component(immediate = true, service = { CapabilityStatementListener.class, RestResourceInitializer.class })
public class RestResourceInitializer implements CapabilityStatementListener, ResourceDefinitionListener {
  private final Map<String, JaxRsServer> servers = new HashMap<>();
  private CapabilityStatement originalCapability;

  @Activate
  private void start() {
    CapabilityStatement cs = CapabilityStatementMonitor.getCapabilityStatement();
    originalCapability = cs == null ? null : cs.copy();
    comply();
  }

  @Deactivate
  private void stop() {
    servers.values().forEach(s -> s.getServerInstance().destroy());
    servers.clear();
  }

  @Override
  public void comply(List<StructureDefinition> definition) {
    comply();
  }

  @Override
  public void comply(CapabilityStatement capabilityStatement) {
    CapabilityStatement cs = CapabilityStatementMonitor.getCapabilityStatement();
    originalCapability = cs == null ? null : cs.copy();
    comply();
  }

  private void comply() {
    new Thread(() -> {
      // hack cxf when trying to load by class name from wrong classloader
      ClassLoaderUtils.setThreadContextClassloader(RestResourceInitializer.class.getClassLoader());
      reloadRest();
    }).run();
  }

  private void reloadRest() {
    stop();
    CapabilityStatement capabilityStatement = getCapability();
    if (capabilityStatement == null) {
      return;
    }
    for (CapabilityStatementRestComponent rest : capabilityStatement.getRest()) {
      if (rest.getMode() == RestfulCapabilityMode.SERVER) {
        start(rest);
      }
    }
  }

  // XXX: unimplemented stuff. changes it also.
  private CapabilityStatement getCapability() {
    CapabilityStatement capabilityStatement = CapabilityStatementMonitor.getCapabilityStatement();
    if (capabilityStatement == null) {
      return null;
    }
    capabilityStatement.setRest(originalCapability.getRest());
    capabilityStatement.setText(null);
    List<String> defined = ResourceDefinitionsMonitor.get().stream().map(d -> d.getName()).collect(toList());
    capabilityStatement.getRest().forEach(rest -> {
      rest.setResource(rest.getResource().stream().filter(rr -> defined.contains(rr.getType())).collect(toList()));
    });
    capabilityStatement.setAcceptUnknown(UnknownContentCode.NO);// no extensions
    capabilityStatement.getRest().forEach(rest -> {
      rest.setOperation(new ArrayList<>());
      rest.setInteraction(new ArrayList<>()); // add transactions and batch some day
      rest.getResource().forEach(rr -> {
        rr.setConditionalCreate(false);
        rr.setConditionalUpdate(false);
        rr.setConditionalDelete(ConditionalDeleteStatus.NOTSUPPORTED);
        rr.setReferencePolicy(Collections.emptyList());
        rr.setSearchInclude(Collections.emptyList());
        rr.setSearchRevInclude(Collections.emptyList());
      });
    });

    return capabilityStatement;
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
    server.createServer();
    servers.put(type, server);
  }

  public Map<String, JaxRsServer> getServers() {
    return servers;
  }

}
