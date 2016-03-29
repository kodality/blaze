package com.nortal.blaze.search.model;

public class Blindex {
  public static final String PIZZELLE = "pizzelle";
  public static final String PARASOL = "parasol";

  private String resourceType;
  private String path;
  private String name;

  public Blindex() {
    //
  }

  public Blindex(String resourceType, String path) {
    this.resourceType = resourceType;
    this.path = path;
  }

  public String getKey() {
    return resourceType + "." + path;
  }

  public String getName() {
    return name;
  }

  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public void setName(String name) {
    this.name = name;
  }
}
