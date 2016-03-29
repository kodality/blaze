package com.nortal.blaze.representation;

import com.nortal.blaze.representation.defs.ResourceRepresentation;
import com.nortal.blaze.representation.defs.ResourceRepresentationComposer;
import org.hl7.fhir.instance.model.Resource;

public final class ResourceComposer {

  private ResourceComposer() {
    //
  }

  public static String compose(Resource resource) {
    return compose(resource, null);
  }

  public static String compose(Resource resource, String mime) {
    ResourceRepresentationComposer composer = ResourceRepresentation.getComposer(mime);
    if (composer == null) {
      composer = ResourceRepresentation.getComposer("json");
    }
    return composer.compose(resource);
  }

}
