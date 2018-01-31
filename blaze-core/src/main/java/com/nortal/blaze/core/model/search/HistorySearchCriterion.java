package com.nortal.blaze.core.model.search;

public class HistorySearchCriterion {
  //  public static final String _COUNT = "_count";
  public static final String _SINCE = "_since";

  private String resourceType;
  private String resourceId;
  //  private Integer count = 100;
  private String since;

  public HistorySearchCriterion() {
    //
  }

  public HistorySearchCriterion(String resourceType) {
    this.resourceType = resourceType;
  }

  public HistorySearchCriterion(String resourceType, String resourceId) {
    this.resourceType = resourceType;
    this.resourceId = resourceId;
  }

  public String getResourceType() {
    return resourceType;
  }

  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }

  public String getResourceId() {
    return resourceId;
  }

  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  public String getSince() {
    return since;
  }

  public void setSince(String since) {
    this.since = since;
  }

  //  public Integer getCount() {
  //    return count;
  //  }
  //
  //  public void setCount(Integer count) {
  //    this.count = count;
  //  }

}
