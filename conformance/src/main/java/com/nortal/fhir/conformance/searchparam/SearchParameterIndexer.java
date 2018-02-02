package com.nortal.fhir.conformance.searchparam;

import com.nortal.blaze.core.api.ResourceIndexer;

/**
 * Implement this to index search parameters during resource save
 */
public abstract class SearchParameterIndexer implements ResourceIndexer {
  // XXX needed?
  // public abstract void index(ResourceId id, List<String> values, SearchParameter searchParam);
  //
  // public abstract void delete(ResourceId id);
  //
  // @Override
  // public void index(ResourceVersion version) {
  // String type = version.getId().getResourceType();
  // List<SearchParameter> searchParameters = SearchParameterMonitor.get(type);
  // if (searchParameters == null) {
  // return;
  // }
  // for (SearchParameter searchParameter : searchParameters) {
  // List<String> values = FhirXpath.text(version.getContent().getValue(), searchParameter.getXpath());
  // index(version.getId(), values, searchParameter);
  // }
  // }

}
