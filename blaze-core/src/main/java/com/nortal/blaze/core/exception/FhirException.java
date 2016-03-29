package com.nortal.blaze.core.exception;

public class FhirException extends RuntimeException {
  private final int statusCode;
  private final String detail;
  private String location;

  public FhirException(int statusCode, Throwable cause) {
    super(cause);
    this.statusCode = statusCode;
    this.detail = cause.getMessage();
  }

  public FhirException(Throwable cause) {
    this(500, cause);
  }

  public FhirException(int statusCode, String detail) {
    this.statusCode = statusCode;
    this.detail = detail;
  }

  public FhirException location(String location) {
    this.location = location;
    return this;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public String getDetail() {
    return detail;
  }

  public String getLocation() {
    return location;
  }

}
