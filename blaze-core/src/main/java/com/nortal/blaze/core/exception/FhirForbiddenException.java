package com.nortal.blaze.core.exception;

public class FhirForbiddenException extends FhirException {

  public FhirForbiddenException(String detail) {
    super(403, detail);
  }

}
