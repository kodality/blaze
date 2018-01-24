package com.nortal.blaze.fhir.structure.defs;

import com.nortal.blaze.fhir.structure.api.ParseException;
import com.nortal.blaze.fhir.structure.api.ResourceRepresentation;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.elementmodel.Manager.FhirFormat;
import org.hl7.fhir.dstu3.formats.JsonParser;
import org.hl7.fhir.dstu3.model.Resource;
import org.osgi.service.component.annotations.Component;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

@Component(immediate = true, service = ResourceRepresentation.class)
public class JsonRepresentation implements ResourceRepresentation {

  @Override
  public List<String> getMimeTypes() {
    return Arrays.asList("application/fhir+json", "application/json+fhir", "application/json", "text/json", "json");
  }
  
  @Override
  public FhirFormat getFhirFormat() {
    return FhirFormat.JSON;
  }
  
  @Override
  public String compose(Resource resource) {
    try {
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      new JsonParser().compose(output, resource);
      return new String(output.toByteArray(), "UTF-8");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean isParsable(String input) {
    String strip = StringUtils.stripStart(input, null);
    return StringUtils.startsWithAny(strip, "{", "[");
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R extends Resource> R parse(String input) {
    try {
      return (R) new JsonParser().parse(input);
    } catch (Exception e) {
      throw new ParseException(e);
    }
  }

}