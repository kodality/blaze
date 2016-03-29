package com.nortal.blaze.representation.defs.xml;

import com.nortal.blaze.representation.defs.ResourceRepresentationComposer;
import java.io.ByteArrayOutputStream;
import org.hl7.fhir.instance.model.Resource;

public class XmlComposer implements ResourceRepresentationComposer {

  @Override
  public String compose(Resource resource) {
    try {
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      new org.hl7.fhir.instance.formats.XmlParser().compose(output, resource, true);
      return new String(output.toByteArray(), "UTF-8");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}