package com.nortal.blaze.core.exception;


public class FhirReferenceException extends FhirException {

  public FhirReferenceException(String detail) {
    super(422, detail);
  }

}
