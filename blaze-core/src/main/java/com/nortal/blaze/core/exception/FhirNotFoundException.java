package com.nortal.blaze.core.exception;

public class FhirNotFoundException extends FhirException {

  public FhirNotFoundException(String detail) {
    super(404, detail);
  }

}
