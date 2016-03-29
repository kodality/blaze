package com.nortal.blaze.representation;

import com.nortal.blaze.representation.defs.ResourceRepresentation;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.hl7.fhir.instance.model.Resource;

public final class ResourceParser {
  private ResourceParser() {
    //
  }

  public static <R extends Resource> R parse(File file) {
    try {
      return parse(FileUtils.readFileToString(file));
    } catch (IOException e) {
      throw new ParseException(e);
    }
  }

  public static <R extends Resource> R parse(String input) {
    return ResourceRepresentation.getParser(input).parse(input);
  }

  public static List<String> xpath(String input, String xpath) {
    return ResourceRepresentation.getParser(input).xpath(input, xpath);
  }

}
