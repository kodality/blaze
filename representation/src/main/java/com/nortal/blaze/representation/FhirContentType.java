package com.nortal.blaze.representation;

import com.nortal.blaze.representation.defs.RepresentationType;
import java.util.ArrayList;
import java.util.List;

public final class FhirContentType {
  private FhirContentType() {
    //
  }

  public static List<String> getMediaTypes() {
    List<String> result = new ArrayList<>();
    for (RepresentationType representation : RepresentationType.values()) {
      result.addAll(representation.getMediaTypes());
    }
    return result;
  }

  public static String getMimeType(String type) {
    for (RepresentationType representation : RepresentationType.values()) {
      if (representation.getMimeTypes().contains(type)) {
        return representation.getMimeTypes().get(0);
      }
    }
    return null;
  }

}
