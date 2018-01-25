package com.nortal.blaze.fhir.structure.service;

import com.nortal.blaze.fhir.structure.api.ParseException;
import com.nortal.blaze.fhir.structure.api.ResourceRepresentation;
import com.nortal.blaze.fhir.structure.defs.JsonRepresentation;
import com.nortal.blaze.fhir.structure.defs.XmlRepresentation;
import org.apache.commons.lang3.StringUtils;
// import org.apache.felix.scr.annotations.Reference;
// import org.apache.felix.scr.annotations.ReferenceCardinality;
// import org.apache.felix.scr.annotations.ReferencePolicy;
import org.hl7.fhir.dstu3.model.Resource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Optional;
import java.util.stream.Stream;

@Component(immediate = true, service = ResourceRepresentationService.class)
public class ResourceRepresentationService {
  // @Reference(policy = ReferencePolicy.DYNAMIC)
  // private final List<ResourceRepresentation> presenters = new ArrayList<>();
  @Reference
  private JsonRepresentation jsonRepresentation;
  @Reference
  private XmlRepresentation xmlRepresentation;

  public String compose(Resource resource, String mime) {
    if (resource == null) {
      return null;
    }
    ResourceRepresentation presenter =
        findPresenter(mime).orElse(findPresenter("json").orElseThrow(() -> new ParseException("unknown format")));
    return presenter.compose(resource);
  }

  public <R extends Resource> R parse(String input) {
    return guessPresenter(input).orElseThrow(() -> new ParseException("unknown format: [" + StringUtils.left(input, 10)
        + "]")).parse(input);
  }

  public Optional<ResourceRepresentation> findPresenter(String mime) {
    if (mime == null) {
      return null;
    }
    return Stream.of(jsonRepresentation, xmlRepresentation).filter(c -> c.getMimeTypes().contains(mime)).findFirst();
    // return presenters.stream().filter(c -> c.getMimeTypes().contains(mime)).findFirst();
  }

  private Optional<ResourceRepresentation> guessPresenter(String content) {
    return Stream.of(jsonRepresentation, xmlRepresentation).filter(c -> c.isParsable(content)).findFirst();
    // return presenters.stream().filter(c -> c.isParsable(content)).findFirst();
  }

  // protected void bind(ResourceRepresentation presenter) {
  // presenters.add(presenter);
  // }
  //
  // protected void unbind(ResourceRepresentation presenter) {
  // presenters.remove(presenter);
  // }

}
