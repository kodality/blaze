package com.nortal.blaze.core.model;

import java.io.Serializable;
import org.apache.commons.lang3.Validate;

public class ResourceId implements Serializable {
  private String resourceId;
  private String resourceType;

  public ResourceId(String resourceType) {
    Validate.notNull(resourceType);
    this.resourceType = resourceType;
  }

  public ResourceId(String resourceType, String resourceId) {
    this(resourceType);
    this.resourceId = resourceId;
  }

  public String getResourceReference() {
    return resourceType + "/" + resourceId;
  }

  public String getReference() {
    return getResourceReference();
  }

  public String getResourceId() {
    return resourceId;
  }

  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  public String getResourceType() {
    return resourceType;
  }

  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }

}
