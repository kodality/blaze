package com.nortal.blaze.representation.defs.json;

import com.nortal.blaze.representation.defs.ResourceRepresentationComposer;
import java.io.ByteArrayOutputStream;
import org.hl7.fhir.instance.model.Resource;

public class JsonComposer implements ResourceRepresentationComposer {

  @Override
  public String compose(Resource resource) {
    try {
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      new org.hl7.fhir.instance.formats.JsonParser().compose(output, resource);
      return new String(output.toByteArray(), "UTF-8");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}