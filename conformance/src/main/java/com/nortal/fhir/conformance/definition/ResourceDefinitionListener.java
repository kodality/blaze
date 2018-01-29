package com.nortal.fhir.conformance.definition;

import java.util.List;
import org.hl7.fhir.dstu3.model.StructureDefinition;

public interface ResourceDefinitionListener {
  void comply(List<StructureDefinition> definition);
}
