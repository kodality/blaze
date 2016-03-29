package com.nortal.blaze.representation.defs.xml;

import com.nortal.blaze.representation.ParseException;
import com.nortal.blaze.representation.defs.ResourceRepresentationParser;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.Resource;

public class XmlParser implements ResourceRepresentationParser {
  @Override
  public boolean isParsable(String input) {
    String strip = StringUtils.stripStart(input, null);
    return StringUtils.startsWith(strip, "<");
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R extends Resource> R parse(String input) {
    try {
      return (R) new org.hl7.fhir.instance.formats.XmlParser().parse(input);
    } catch (Exception e) {
      throw new ParseException(e);
    }
  }

  @Override
  public List<String> xpath(String input, String xpath) {
    return FhirXpath.text(input, xpath);
  }
}