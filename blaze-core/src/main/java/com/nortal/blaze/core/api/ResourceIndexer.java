package com.nortal.blaze.core.api;

import com.nortal.blaze.core.model.ResourceId;
import com.nortal.blaze.core.model.ResourceVersion;

public interface ResourceIndexer {
  void index(ResourceVersion version);

  void delete(ResourceId id);
}
