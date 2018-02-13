package com.nortal.blaze.core.api.resource;

import com.nortal.blaze.core.model.ResourceContent;
import com.nortal.blaze.core.model.ResourceVersion;
import com.nortal.blaze.core.model.VersionId;

public interface ResourceSaveHandler {
  ResourceContent beforeSave(VersionId id, ResourceContent content);

  void afterSave(ResourceVersion savedVersion);
}
