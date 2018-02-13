package com.nortal.blaze.core.api.conformance;

import java.util.List;
import org.hl7.fhir.dstu3.model.StructureDefinition;

public interface ResourceDefinitionListener {
  void comply(List<StructureDefinition> definition);
}