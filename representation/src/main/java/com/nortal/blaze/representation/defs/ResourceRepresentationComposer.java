package com.nortal.blaze.representation.defs;

import org.hl7.fhir.instance.model.Resource;

public interface ResourceRepresentationComposer {
  String compose(Resource resource);
}