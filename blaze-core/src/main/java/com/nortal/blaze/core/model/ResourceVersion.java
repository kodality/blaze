package com.nortal.blaze.core.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public class ResourceVersion implements Serializable {
  private VersionId id;
  private ResourceContent content;

  private Map<String, Object> author;
  private Date modified;
  private boolean deleted;

  public ResourceVersion() {
    //
  }

  public ResourceVersion(VersionId id, ResourceContent content) {
    this.id = id;
    this.content = content;
  }

  public String getReference() {
    return getId().getReference();
  }

  public String getETag() {
    return getId().getETag();
  }

  public VersionId getId() {
    return id;
  }

  public void setId(VersionId id) {
    this.id = id;
  }

  public ResourceContent getContent() {
    return content;
  }

  public void setContent(ResourceContent content) {
    this.content = content;
  }

  public Date getModified() {
    return modified;
  }

  public void setModified(Date modified) {
    this.modified = modified;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  public Map<String, Object> getAuthor() {
    return author;
  }

  public void setAuthor(Map<String, Object> author) {
    this.author = author;
  }

}
