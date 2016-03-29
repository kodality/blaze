package com.nortal.fhir.rest.server;

import org.hl7.fhir.instance.model.Conformance.ConformanceRestResourceComponent;

public interface FhirResourceServerFactory {

  String getType();

  JaxRsServer construct(ConformanceRestResourceComponent conformance);
}
