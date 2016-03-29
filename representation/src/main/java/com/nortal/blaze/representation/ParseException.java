package com.nortal.blaze.representation;

public class ParseException extends RuntimeException {
  public ParseException(String message) {
    super(message);
  }

  public ParseException(Throwable cause) {
    super(cause);
  }

}
