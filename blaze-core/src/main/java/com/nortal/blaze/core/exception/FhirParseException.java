package com.nortal.blaze.core.exception;

public class FhirParseException extends FhirException {

  public FhirParseException(String detail) {
    super(400, detail);
  }

  public FhirParseException(Throwable cause) {
    super(400, cause);
  }

}
