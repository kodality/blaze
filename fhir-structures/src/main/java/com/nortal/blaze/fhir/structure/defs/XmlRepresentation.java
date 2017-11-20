package com.nortal.blaze.fhir.structure.defs;

import com.nortal.blaze.fhir.structure.api.ParseException;
import com.nortal.blaze.fhir.structure.api.ResourceRepresentation;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.formats.XmlParser;
import org.hl7.fhir.dstu3.model.Resource;
import org.osgi.service.component.annotations.Component;

@Component(immediate = true, service = ResourceRepresentation.class)
public class XmlRepresentation implements ResourceRepresentation {

  @Override
  public List<String> getMimeTypes() {
    return Arrays.asList("application/fhir+xml", "application/xml+fhir", "application/xml", "text/xml", "xml");
  }

  @Override
  public String compose(Resource resource) {
    try {
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      new XmlParser().compose(output, resource, true);
      return new String(output.toByteArray(), "UTF-8");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean isParsable(String input) {
    String strip = StringUtils.stripStart(input, null);
    return StringUtils.startsWith(strip, "<");
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R extends Resource> R parse(String input) {
    try {
      return (R) new XmlParser().parse(input);
    } catch (Exception e) {
      throw new ParseException(e);
    }
  }

}