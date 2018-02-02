package com.nortal.fhir.rest.root;

import com.nortal.blaze.core.model.VersionId;
import com.nortal.blaze.core.service.ResourceService;
import com.nortal.blaze.core.util.ResourceUtil;
import com.nortal.fhir.rest.util.ResourcePropertyUtil;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryRequestComponent;
import org.hl7.fhir.dstu3.model.Bundle.HTTPVerb;
import org.hl7.fhir.dstu3.model.Reference;
import org.osgi.service.component.annotations.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Component(immediate = true, service = BundleReferenceHandler.class)
public class BundleReferenceHandler {
  @org.osgi.service.component.annotations.Reference
  private ResourceService resourceService;

  public void replaceIds(Bundle bundle) {
    // fullUrl -> local key
    Map<String, String> referenceIds = new HashMap<>();
    bundle.getEntry().forEach(entry -> {
      BundleEntryRequestComponent request = entry.getRequest();
      if (request.getMethod() == HTTPVerb.PUT) {
        VersionId id = ResourceUtil.parseReference(request.getUrl());
        referenceIds.put(entry.getFullUrl(), id.getResourceReference());
      }
      if (request.getMethod() == HTTPVerb.POST) {
        String newId = resourceService.prepareId();
        String ref = entry.getResource().getResourceType() + "/" + newId;
        referenceIds.put(entry.getFullUrl(), ref);

        //XXX not sure if it is good idea to replace method, but how to get id before i save in this case?
        request.setUrl(ref);
        request.setMethod(HTTPVerb.PUT);
      }
    });

    ResourcePropertyUtil.findProperties(bundle, new HashSet<>(), Reference.class).forEach(reference -> {
      reference.setReference(referenceIds.get(reference.getReference()));
    });
  }
}
