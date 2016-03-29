package com.nortal.blaze.search.model;

public class StructureElement {
  private final String path;
  private final String[] types;
  private boolean isMany;

  public StructureElement(String path, String[] types) {
    this.path = path;
    this.types = types;
  }

  public String getPath() {
    return path;
  }

  public String[] getTypes() {
    return types;
  }

  public boolean isMany() {
    return isMany;
  }

  public void setMany(boolean isMany) {
    this.isMany = isMany;
  }

}
