package com.nortal.blaze.core.exception;

public class FhirBadRequestException extends FhirException {

  public FhirBadRequestException(String detail) {
    super(400, detail);
  }

}
