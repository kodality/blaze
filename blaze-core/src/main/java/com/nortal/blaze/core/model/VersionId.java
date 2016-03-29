package com.nortal.blaze.core.model;

public class VersionId extends ResourceId {
  private Integer version;

  public VersionId(String resourceType) {
    super(resourceType);
  }

  public VersionId(String resourceType, String resourceId) {
    super(resourceType, resourceId);
  }

  public VersionId(ResourceId id) {
    this(id.getResourceType(), id.getResourceId());
  }

  public VersionId(String resourceType, String resourceId, Integer version) {
    this(resourceType, resourceId);
    this.version = version;
  }

  @Override
  public String getReference() {
    if (version == null) {
      return super.getReference();
    }
    return super.getReference() + "/_history/" + version;
  }

  public String getETag() {
    if (version == null) {
      return "";
    }
    return "W/\"" + version + "\"";
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

}
