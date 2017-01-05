package com.nortal.fhir.conformance.operations;

import com.nortal.blaze.core.iface.ResourceIndexer;
import com.nortal.blaze.core.model.ResourceId;
import com.nortal.blaze.core.model.ResourceVersion;
import com.nortal.blaze.fhir.structure.api.FhirXpath;
import java.util.List;
import org.hl7.fhir.dstu3.model.SearchParameter;

/**
 * Implement this to index search parameters during resource save
 */
public abstract class SearchParameterIndexer implements ResourceIndexer {
  public abstract void index(ResourceId id, List<String> values, SearchParameter searchParam);

  public abstract void delete(ResourceId id);

  @Override
  public void index(ResourceVersion version) {
    String type = version.getId().getResourceType();
    List<SearchParameter> searchParameters = SearchParameterMonitor.get(type);
    if (searchParameters == null) {
      return;
    }
    for (SearchParameter searchParameter : searchParameters) {
      List<String> values =  FhirXpath.text(version.getContent().getValue(), searchParameter.getXpath());
      index(version.getId(), values, searchParameter);
    }
  }

}
