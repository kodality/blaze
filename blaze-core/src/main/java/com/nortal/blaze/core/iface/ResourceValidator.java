package com.nortal.blaze.core.iface;

import com.nortal.blaze.core.model.ResourceContent;

public interface ResourceValidator {

  void validate(String type, ResourceContent content);
}
