package com.nortal.fhir.conformance.operations;

import org.hl7.fhir.instance.model.Conformance;

public interface ConformanceListener {
  void comply(Conformance conformance);
}
