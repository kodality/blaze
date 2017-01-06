package com.nortal.blaze.fhir.structure.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;

@Component(immediate = true)
@Service(FhirContentType.class)
@Reference(name = "presenters", cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, referenceInterface = ResourceRepresentation.class, policy = ReferencePolicy.DYNAMIC)
public class FhirContentType {
  private static final Map<String, String> mimes = new HashMap<>();
  private static final List<String> mediaTypes = new ArrayList<>();

  public static List<String> getMediaTypes() {
    return mediaTypes;
  }

  public static String getMimeType(String type) {
    return mimes.get(type);
  }

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
