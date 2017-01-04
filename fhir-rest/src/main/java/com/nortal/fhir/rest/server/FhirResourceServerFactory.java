package com.nortal.fhir.rest.server;

import org.hl7.fhir.dstu3.model.CapabilityStatement.CapabilityStatementRestResourceComponent;

public interface FhirResourceServerFactory {

  String getType();

  JaxRsServer construct(CapabilityStatementRestResourceComponent conformance);
}
