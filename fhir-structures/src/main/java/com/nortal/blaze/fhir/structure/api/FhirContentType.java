package com.nortal.blaze.fhir.structure.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(immediate = true, service = FhirContentType.class)
public class FhirContentType {
  private static final Map<String, String> mimes = new HashMap<>();
  private static final List<String> mediaTypes = new ArrayList<>();

  public static List<String> getMediaTypes() {
    return mediaTypes;
  }

  public static String getMimeType(String type) {
    return mimes.get(type);
  }

  @Reference(name = "presenters", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, unbind = "unbind")
  protected void bind(ResourceRepresentation presenter) {
    String main = presenter.getMimeTypes().get(0);
    presenter.getMimeTypes().forEach(mime -> {
      if (mimes.containsKey(mime)) {
        throw new IllegalStateException(" multiple composers for mime " + mime);
      }
      mimes.put(mime, main);
      if (mime.contains("/")) {
        mediaTypes.add(mime);
      }
    });
  }

  protected void unbind(ResourceRepresentation presenter) {
    presenter.getMimeTypes().forEach(mime -> {
      mimes.remove(mime);
      mediaTypes.remove(mime);
    });
  }

}
