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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.common.classloader.ClassLoaderUtils;
import org.hl7.fhir.dstu3.model.CapabilityStatement;
import org.hl7.fhir.dstu3.model.CapabilityStatement.CapabilityStatementRestComponent;
import org.hl7.fhir.dstu3.model.CapabilityStatement.CapabilityStatementRestResourceComponent;
import org.hl7.fhir.dstu3.model.CapabilityStatement.ConditionalDeleteStatus;
import org.hl7.fhir.dstu3.model.CapabilityStatement.RestfulCapabilityMode;
import org.hl7.fhir.dstu3.model.CapabilityStatement.SystemRestfulInteraction;
import org.hl7.fhir.dstu3.model.CapabilityStatement.UnknownContentCode;
import org.hl7.fhir.dstu3.model.StructureDefinition;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Component(immediate = true, service = { CapabilityStatementListener.class, ResourceDefinitionListener.class,
                                         RestResourceInitializer.class })
public class RestResourceInitializer implements CapabilityStatementListener, ResourceDefinitionListener {
  private final Map<String, JaxRsServer> servers = new HashMap<>();
  private CapabilityStatement modifiedCapability;

  @Activate
  public void restart() {
    modifiedCapability =
        modifyCapability(CapabilityStatementMonitor.getCapabilityStatement(), ResourceDefinitionsMonitor.get());
    comply();
  }

  @Deactivate
  private void stop() {
    servers.values().forEach(s -> s.getServerInstance().destroy());
    servers.clear();
  }

  @Override
  public void comply(List<StructureDefinition> definition) {
    modifiedCapability = modifyCapability(CapabilityStatementMonitor.getCapabilityStatement(), definition);
    comply();
  }

  @Override
  public void comply(CapabilityStatement capabilityStatement) {
    modifiedCapability = modifyCapability(capabilityStatement, ResourceDefinitionsMonitor.get());
    comply();
  }

  public CapabilityStatement getModifiedCapability() {
    return modifiedCapability;
  }

  private void comply() {
    new Thread(() -> {
      // hack cxf when trying to load by class name from wrong classloader
      ClassLoaderUtils.setThreadContextClassloader(RestResourceInitializer.class.getClassLoader());
      reloadRest();
    }).run();
  }

  private synchronized void reloadRest() {
    stop();
    if (modifiedCapability == null) {
      return;
    }
    for (CapabilityStatementRestComponent rest : modifiedCapability.getRest()) {
      if (rest.getMode() == RestfulCapabilityMode.SERVER) {
        start(rest);
      }
    }
  }

  // XXX: unimplemented stuff
  private CapabilityStatement modifyCapability(CapabilityStatement capabilityStatement,
                                               List<StructureDefinition> definitions) {
    if (capabilityStatement == null || CollectionUtils.isEmpty(definitions)) {
      return null;
    }
    capabilityStatement = capabilityStatement.copy();
    capabilityStatement.setText(null);
    List<String> defined = definitions.stream().map(d -> d.getName()).collect(toList());
    defined.removeAll(Arrays.asList("Bundle",
                                    "Binary",
                                    "CapabilityStatement",
                                    "StructureDefinition",
                                    "ImplementationGuide",
                                    "SearchParameter",
                                    "ImplementationGuide",
                                    "MessageDefinition",
                                    "OperationDefinition",
                                    "CompartmentDefinition",
                                    "StructureMap",
                                    "GraphDefinition",
                                    "DataElement",
                                    "AuditEvent"));
    capabilityStatement.getRest().forEach(rest -> {
      rest.setResource(rest.getResource().stream().filter(rr -> defined.contains(rr.getType())).collect(toList()));
    });
    capabilityStatement.setAcceptUnknown(UnknownContentCode.NO);// no extensions
    capabilityStatement.getRest().forEach(rest -> {
      rest.setOperation(null);
      List<String> interactions =
          Arrays.asList("transaction", "batch", SystemRestfulInteraction.HISTORYSYSTEM.toCode());
      rest.setInteraction(rest.getInteraction().stream().filter(i -> interactions.contains(i.getCode().toCode())).collect(toList()));
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
    start("$root", new FhirRootServer(rest));
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
