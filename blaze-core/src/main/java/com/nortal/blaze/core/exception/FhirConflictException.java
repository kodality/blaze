package com.nortal.blaze.core.exception;

public class FhirConflictException extends FhirException {

  public FhirConflictException(String detail) {
    super(409, detail);
  }

}
