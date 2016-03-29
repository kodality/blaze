package com.nortal.blaze.core.service;

import com.nortal.blaze.core.model.ResourceContent;
import com.nortal.blaze.core.model.ResourceId;
import com.nortal.blaze.core.model.ResourceVersion;
import com.nortal.blaze.core.model.VersionId;
import com.nortal.blaze.core.model.search.SearchCriterion;
import com.nortal.blaze.core.model.search.SearchResult;
import java.util.List;

public interface ResourceService {

  ResourceVersion save(VersionId id, ResourceContent content);

  void delete(ResourceId id);

  ResourceVersion load(VersionId id);

  List<ResourceVersion> loadHistory(ResourceId id);

  SearchResult search(SearchCriterion criteria);

}
