package com.nortal.blaze.core.api.resource;

import com.nortal.blaze.core.model.ResourceContent;

public interface ResourceValidator {

  void validate(String type, ResourceContent content);
}
