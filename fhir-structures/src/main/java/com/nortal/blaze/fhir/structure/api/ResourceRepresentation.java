package com.nortal.blaze.fhir.structure.api;

import java.util.List;
import org.hl7.fhir.dstu3.model.Resource;

public interface ResourceRepresentation {
  List<String> getMimeTypes();

  String compose(Resource resource);

  boolean isParsable(String input);

  <R extends Resource> R parse(String input);
}