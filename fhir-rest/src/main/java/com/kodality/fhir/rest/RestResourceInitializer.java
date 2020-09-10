/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kodality.fhir.rest;

import com.kodality.blaze.core.api.conformance.CapabilityStatementListener;
import com.kodality.blaze.core.api.conformance.ResourceDefinitionListener;
import com.kodality.blaze.core.service.conformance.ConformanceHolder;
import com.kodality.fhir.rest.server.FhirResourceServer;
import com.kodality.fhir.rest.server.FhirResourceServerFactory;
import com.kodality.fhir.rest.server.FhirRootServer;
import com.kodality.fhir.rest.server.JaxRsServer;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestResourceComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.RestfulCapabilityMode;
import org.hl7.fhir.r4.model.CapabilityStatement.SystemRestfulInteraction;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.osgi.service.component.annotations.ReferenceCardinality.MULTIPLE;
import static org.osgi.service.component.annotations.ReferencePolicy.DYNAMIC;

@Component(immediate = true, service = { CapabilityStatementListener.class, ResourceDefinitionListener.class,
                                         RestResourceInitializer.class })
public class RestResourceInitializer implements CapabilityStatementListener, ResourceDefinitionListener {

  private final List<FhirResourceServerFactory> customServers = new ArrayList<>();

  private final Map<String, JaxRsServer> servers = new HashMap<>();
  private CapabilityStatement capability;

  @Activate
  public void init() {
    capability = modifyCapability(ConformanceHolder.getCapabilityStatement(), ConformanceHolder.getDefinitions());
    restart();
  }

  @Deactivate
  private void stop() {
    servers.values().forEach(s -> s.getServerInstance().destroy());
    servers.clear();
  }

  @Override
  public void comply(List<StructureDefinition> definition) {
    capability = modifyCapability(ConformanceHolder.getCapabilityStatement(), definition);
    restart();
  }

  @Override
  public void comply(CapabilityStatement capabilityStatement) {
    capability = modifyCapability(capabilityStatement, ConformanceHolder.getDefinitions());
    restart();
  }

  public CapabilityStatement getModifiedCapability() {
    return capability;
  }

  private void restart() {
    stop();
    if (capability != null) {
      capability.getRest().forEach(rest -> {
        if (rest.getMode() == RestfulCapabilityMode.SERVER) {
          start(rest);
        }
      });
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
    //    capabilityStatement.setAcceptUnknown(UnknownContentCode.NO);// no extensions
    //XXX r3 -> r4
    //    src.acceptUnknown as vs ->  tgt.extension as ext,  ext.url = 'http://hl7.org/fhir/3.0/StructureDefinition/extension-CapabilityStatement.acceptUnknown',  ext.value = vs;
    capabilityStatement.getRest().forEach(rest -> {
      rest.setOperation(null);
      List<String> interactions =
          Arrays.asList("transaction", "batch", SystemRestfulInteraction.HISTORYSYSTEM.toCode());
      rest.setInteraction(rest.getInteraction()
          .stream()
          .filter(i -> interactions.contains(i.getCode().toCode()))
          .collect(toList()));
      rest.getResource().forEach(rr -> {
        //        rr.setConditionalCreate(false);
        //        rr.setConditionalUpdate(false);
        //        rr.setConditionalDelete(ConditionalDeleteStatus.NOTSUPPORTED);
        rr.setReferencePolicy(Collections.emptyList());
        //        rr.setSearchInclude(Collections.emptyList());
        //        rr.setSearchRevInclude(Collections.emptyList());
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
    if (servers.containsKey(type)) {
      return;
    }
    for (FhirResourceServerFactory factory : customServers) {
      if (StringUtils.equals(factory.getType(), type)) {
        start(type, factory.construct(resourceRest));
        return;
      }
    }
    start(type, new FhirResourceServer(resourceRest));
  }

  public void start(String type, JaxRsServer server) {
    if (servers.containsKey(type)) {
      servers.get(type).getServerInstance().destroy();
      servers.remove(type);
    }
    server.createServer();
    servers.put(type, server);
  }

  public Map<String, JaxRsServer> getServers() {
    return servers;
  }

  @Reference(cardinality = MULTIPLE, policy = DYNAMIC, service = FhirResourceServerFactory.class, name = "FhirResourceServerFactory")
  protected void bind(FhirResourceServerFactory factory) {
    this.customServers.add(factory);
    restart(); //TODO: avoid reloading all services?
  }

  protected void unbind(FhirResourceServerFactory factory) {
    this.customServers.remove(factory);
    restart();
  }

}
