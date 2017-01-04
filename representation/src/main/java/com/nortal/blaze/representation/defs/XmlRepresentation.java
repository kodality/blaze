package com.nortal.blaze.representation.defs;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import com.nortal.blaze.representation.api.ParseException;
import com.nortal.blaze.representation.api.ResourceRepresentation;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.hl7.fhir.dstu3.model.Resource;

@Component(immediate = true)
@Service(ResourceRepresentation.class)
public class XmlRepresentation implements ResourceRepresentation {
  private FhirContext stu;
  
  @Activate
  private void init() {
      stu = new FhirContext(FhirVersionEnum.DSTU3);
//          FhirContext.forDstu3();
  }

  @Override
  public List<String> getMimeTypes() {
    return Arrays.asList("application/xml+fhir", "application/xml", "text/xml", "xml");
  }

  @Override
  public String compose(Resource resource) {
    try {
      return stu.newXmlParser().encodeResourceToString(resource);
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
      return (R) stu.newXmlParser().parseResource(input);
    } catch (Exception e) {
      throw new ParseException(e);
    }
  }

}