package com.nortal.fhir.conformance.operations;

import org.hl7.fhir.dstu3.model.CapabilityStatement;

public interface CapabilityStatementListener {
  void comply(CapabilityStatement capabilityStatement);
}
