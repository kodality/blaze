package com.nortal.blaze.core.model;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

public class ResourceContent implements Serializable {
  private String value;
  private final String contentType;

  public ResourceContent(String value, String contentType) {
    this.value = value;
    this.contentType = contentType;
  }

  public byte[] getBytes() {
    try {
      return value.getBytes("UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getContentType() {
    return contentType;
  }

}
