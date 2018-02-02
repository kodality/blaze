package com.nortal.fhir.rest.feature;

import com.nortal.blaze.core.api.ResourceSaveHandler;
import com.nortal.blaze.core.exception.FhirException;
import com.nortal.blaze.core.model.ResourceContent;
import com.nortal.blaze.core.model.ResourceVersion;
import com.nortal.blaze.core.model.VersionId;
import com.nortal.blaze.core.model.search.SearchCriterion;
import com.nortal.blaze.core.model.search.SearchResult;
import com.nortal.blaze.core.service.ResourceService;
import com.nortal.blaze.fhir.structure.service.ResourceRepresentationService;
import com.nortal.fhir.rest.util.ResourcePropertyUtil;
import com.nortal.fhir.rest.util.SearchUtil;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Resource;
import org.osgi.service.component.annotations.Component;

import java.util.HashSet;

/**
 * https://www.hl7.org/fhir/http.html#transaction 
 * Conditional Reference implementation. 
 * originally should only apply to transaction, but we thought might be global..
 * 
 * TODO: move somewhere more appropriate
 */
@Component(immediate = true, service = ResourceSaveHandler.class)
public class ResourceConditionalReferenceFeature implements ResourceSaveHandler {
  @org.osgi.service.component.annotations.Reference
  private ResourceRepresentationService representationService;
  @org.osgi.service.component.annotations.Reference
  private ResourceService resourceService;

  @Override
  public ResourceContent beforeSave(VersionId id, ResourceContent content) {
    Resource resource = representationService.parse(content.getValue());
    ResourcePropertyUtil.findProperties(resource, new HashSet<>(), Reference.class).forEach(reference -> {
      String uri = reference.getReference();
      if (!uri.contains("?")) {
        return;
      }
      String resourceType = StringUtils.substringBefore(uri, "?");
      String query = StringUtils.substringAfter(uri, "?");

      SearchCriterion criteria = new SearchCriterion(resourceType, SearchUtil.parse(query, resourceType));
      criteria.getResultParams().clear();
      SearchResult result = resourceService.search(criteria);
      if (result.getTotal() != 1) {
        throw new FhirException(400, "found more than one resources by " + uri);
      }
      reference.setReference(result.getEntries().get(0).getId().getResourceReference());
    });
    String newContent = representationService.compose(resource, content.getContentType());
    content.setValue(newContent);
    return content;
  }

  @Override
  public void afterSave(ResourceVersion savedVersion) {
    // nothing
  }

}
