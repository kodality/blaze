package com.nortal.fhir.conformance.content;

import java.util.List;
import org.hl7.fhir.instance.model.StructureDefinition;

public interface ResourceDefinitionListener {
  void comply(List<StructureDefinition> definition);
}
