package com.nortal.blaze.fhir.structure.service;

import com.nortal.blaze.fhir.structure.api.ParseException;
import com.nortal.blaze.fhir.structure.api.ResourceRepresentation;
// import org.apache.felix.scr.annotations.Reference;
// import org.apache.felix.scr.annotations.ReferenceCardinality;
// import org.apache.felix.scr.annotations.ReferencePolicy;
import org.hl7.fhir.dstu3.model.Resource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component(immediate = true, service = ResourceRepresentationService.class)
public class ResourceRepresentationService {
  @Reference(policy = ReferencePolicy.DYNAMIC)
  private final List<ResourceRepresentation> presenters = new ArrayList<>();

  public String compose(Resource resource, String mime) {
    if (resource == null) {
      return null;
    }
    ResourceRepresentation presenter =
        findPresenter(mime).orElse(findPresenter("json").orElseThrow(() -> new ParseException("unknown format")));
    return presenter.compose(resource);
  }

  public <R extends Resource> R parse(String input) {
    return guessPresenter(input).parse(input);
  }

  public Optional<ResourceRepresentation> findPresenter(String mime) {
    if (mime == null) {
      return null;
    }
    return presenters.stream().filter(c -> c.getMimeTypes().contains(mime)).findFirst();
  }

  private ResourceRepresentation guessPresenter(String content) {
    return presenters.stream().filter(c -> c.isParsable(content)).findFirst().orElseThrow(() -> new ParseException("unknown format"));
  }

  protected void bind(ResourceRepresentation presenter) {
    presenters.add(presenter);
  }

  protected void unbind(ResourceRepresentation presenter) {
    presenters.remove(presenter);
  }

}
