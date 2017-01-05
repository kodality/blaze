package com.nortal.blaze.fhir.structure.api;

public class ParseException extends RuntimeException {
  public ParseException(String message) {
    super(message);
  }

  public ParseException(Throwable cause) {
    super(cause);
  }

}
