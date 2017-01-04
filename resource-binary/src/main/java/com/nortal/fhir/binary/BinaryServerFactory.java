package com.nortal.fhir.binary;

import com.nortal.fhir.rest.server.FhirResourceServerFactory;
import com.nortal.fhir.rest.server.JaxRsServer;
import org.hl7.fhir.dstu3.model.CapabilityStatement.CapabilityStatementRestResourceComponent;
import org.hl7.fhir.dstu3.model.ResourceType;

// TODO: make this work
// @Component(immediate = true)
// @Service(FhirResourceServerFactory.class)
public class BinaryServerFactory implements FhirResourceServerFactory {

  @Override
  public String getType() {
    return ResourceType.Binary.name();
  }

  @Override
  public JaxRsServer construct(CapabilityStatementRestResourceComponent capability) {
    return new FhirBinaryRest(capability);
  }

}
