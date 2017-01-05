package com.nortal.blaze.fhir.structure.api;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum RepresentationType {
  XML(new String[] { "application/xml+fhir", "application/xml", "text/xml", "xml" }),
  JSON(new String[] { "application/json+fhir", "application/json", "text/json", "json" });

  private final List<String> mimeTypes;
  private final List<String> mediaTypes;

  private RepresentationType(String[] mimeTypes) {
    this.mimeTypes = Arrays.asList(mimeTypes);
    this.mediaTypes = this.mimeTypes.stream().filter(s -> s.contains("/")).collect(Collectors.toList());
  }

  public List<String> getMimeTypes() {
    return mimeTypes;
  }

  public List<String> getMediaTypes() {
    return mediaTypes;
  }

}
