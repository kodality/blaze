package com.nortal.blaze.core.iface;

import com.nortal.blaze.core.model.ResourceContent;
import com.nortal.blaze.core.model.ResourceId;
import com.nortal.blaze.core.model.ResourceVersion;
import com.nortal.blaze.core.model.VersionId;
import com.nortal.blaze.core.model.search.HistorySearchCriterion;

import java.util.List;

public interface ResourceStorehouse {
  ResourceVersion save(VersionId id, ResourceContent content);

  void delete(ResourceId id);

  ResourceVersion load(VersionId id);

  List<ResourceVersion> loadHistory(HistorySearchCriterion criteria);

}
