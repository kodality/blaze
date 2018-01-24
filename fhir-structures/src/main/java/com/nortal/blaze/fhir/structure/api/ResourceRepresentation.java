package com.nortal.blaze.fhir.structure.api;

import org.hl7.fhir.dstu3.elementmodel.Manager.FhirFormat;
import org.hl7.fhir.dstu3.model.Resource;

import java.util.List;

public interface ResourceRepresentation {
  List<String> getMimeTypes();
  
  FhirFormat getFhirFormat();

  String compose(Resource resource);

  boolean isParsable(String input);

  <R extends Resource> R parse(String input);
}