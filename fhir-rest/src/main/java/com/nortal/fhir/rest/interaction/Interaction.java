package com.nortal.fhir.rest.interaction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Interaction {
  // http://hl7.org/fhir/restful-interaction
  public static final String READ = "read";
  public static final String VREAD = "vread";
  public static final String UPDATE = "update";
  public static final String DELETE = "delete";
  public static final String HISTORYINSTANCE = "history-instance";
  public static final String HISTORYTYPE = "history-type";
  public static final String HISTORYSYSTEM = "history-system";
  public static final String CREATE = "create";
  public static final String SEARCHTYPE = "search-type";
  public static final String SEARCHSYSTEM = "search-system";
  public static final String VALIDATE = "validate";
  public static final String CONFORMANCE = "conformance";
  public static final String TRANSACTION = "transaction";
  public static final String OPERATION = "operation";

  String value();
}
