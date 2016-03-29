package com.nortal.fhir.binary;

import com.nortal.fhir.rest.server.FhirResourceServerFactory;
import com.nortal.fhir.rest.server.JaxRsServer;
import org.hl7.fhir.instance.model.Conformance.ConformanceRestResourceComponent;
import org.hl7.fhir.instance.model.ResourceType;

// TODO: make this work
// @Component(immediate = true)
// @Service(FhirResourceServerFactory.class)
public class BinaryServerFactory implements FhirResourceServerFactory {

  @Override
  public String getType() {
    return ResourceType.Binary.name();
  }

  @Override
  public JaxRsServer construct(ConformanceRestResourceComponent conformance) {
    return new FhirBinaryRest(conformance);
  }

}
