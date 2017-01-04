package com.nortal.blaze.representation.defs;

import ca.uhn.fhir.context.FhirContext;
import com.nortal.blaze.representation.api.ParseException;
import com.nortal.blaze.representation.api.ResourceRepresentation;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Activate;
import org.hl7.fhir.dstu3.model.Resource;

//@Component(immediate = true)
//@Service(ResourceRepresentation.class)
public class JsonRepresentation implements ResourceRepresentation {
  private FhirContext stu;

  @Activate
  private void init() {
    new Thread(() -> {
      stu = FhirContext.forDstu3();
    }).start();
  }

  @Override
  public List<String> getMimeTypes() {
    return Arrays.asList("application/json+fhir", "application/json", "text/json", "json");
  }

  @Override
  public String compose(Resource resource) {
    try {
      return stu.newJsonParser().encodeResourceToString(resource);
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
      return (R) stu.newJsonParser().parseResource(input);
    } catch (Exception e) {
      throw new ParseException(e);
    }
  }

}