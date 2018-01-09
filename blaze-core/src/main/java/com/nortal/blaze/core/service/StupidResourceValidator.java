package com.nortal.blaze.core.service;

import com.nortal.blaze.core.exception.FhirParseException;
import com.nortal.blaze.core.iface.ResourceValidator;
import com.nortal.blaze.core.model.ResourceContent;
import com.nortal.blaze.fhir.structure.service.ResourceRepresentationService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(immediate = true, service = ResourceValidator.class)
public class StupidResourceValidator implements ResourceValidator {
  @Reference
  private ResourceRepresentationService resourceRepresentationService;

  @Override
  public void validate(String type, ResourceContent content) {
    try {
      resourceRepresentationService.parse(content.getValue());
    } catch (Exception e) {
      throw new FhirParseException(e);
    }
  }

}
