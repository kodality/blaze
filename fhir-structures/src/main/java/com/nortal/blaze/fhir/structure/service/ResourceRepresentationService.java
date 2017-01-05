package com.nortal.blaze.fhir.structure.service;

import com.nortal.blaze.fhir.structure.api.ResourceRepresentation;
import java.util.ArrayList;
import java.util.List;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.hl7.fhir.dstu3.model.Resource;

@Component(immediate = true)
@Service(ResourceRepresentationService.class)
public class ResourceRepresentationService {
  @Reference(cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, referenceInterface = ResourceRepresentation.class, policy = ReferencePolicy.DYNAMIC)
  private final List<ResourceRepresentation> presenters = new ArrayList<>();

  public String compose(Resource resource, String mime) {
    if (resource == null) {
      return null;
    }
    ResourceRepresentation presenter = findPresenter(mime);
    if (presenter == null) {
      presenter = findPresenter("json");
    }
    return presenter.compose(resource);
  }

  public <R extends Resource> R parse(String input) {
    return guessPresenter(input).parse(input);
  }

  private ResourceRepresentation findPresenter(String mime) {
    return presenters.stream().filter(c -> c.getMimeTypes().contains(mime)).findFirst().orElse(null);
  }

  private ResourceRepresentation guessPresenter(String content) {
    return presenters.stream().filter(c -> c.isParsable(content)).findFirst().orElse(null);
  }

  protected void bind(ResourceRepresentation presenter) {
    presenters.add(presenter);
  }

  protected void unbind(ResourceRepresentation presenter) {
    presenters.remove(presenter);
  }

}
