package com.nortal.blaze.core.exception;

public class ServerException extends FhirException {

  public ServerException(String detail) {
    super(500, detail);
  }

}
