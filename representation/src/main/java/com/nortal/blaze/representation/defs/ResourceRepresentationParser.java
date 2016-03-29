package com.nortal.blaze.representation.defs;

import java.util.List;
import org.hl7.fhir.instance.model.Resource;

public interface ResourceRepresentationParser {
  boolean isParsable(String input);

  <R extends Resource> R parse(String input);

  List<String> xpath(String input, String xpath);
}