package com.nortal.fhir.rest.exception;

import com.nortal.blaze.core.exception.FhirException;
import com.nortal.blaze.fhir.structure.api.ResourceComposer;
import com.nortal.fhir.rest.filter.RequestContext;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hl7.fhir.dstu3.model.OperationOutcome;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;

import static java.util.stream.Collectors.joining;

public class FhirExceptionHandler implements ExceptionMapper<Throwable> {
  private final static Logger LOG = LogManager.getLogger(FhirExceptionHandler.class);

  @Override
  public Response toResponse(Throwable e) {
    return getResponse(e);
  }

  public static Response getResponse(Throwable e) {
    Throwable root = ExceptionUtils.getRootCause(e);

    if (e instanceof FhirException) {
      return toResponse((FhirException) e);
    }
    if (root instanceof FhirException) {
      return toResponse((FhirException) root);
    }

    LOG.error("hello", e);
    if (e instanceof WebApplicationException) {
      return ((WebApplicationException) e).getResponse();
    }

    String stackTrace = ExceptionUtils.getStackTrace(e);
    return Response.serverError().entity(stackTrace).build();
  }

  private static Response toResponse(FhirException e) {
    log(e);

    ResponseBuilder response = Response.status(e.getStatusCode());
    if (e.getIssues() != null) {
      OperationOutcome outcome = new OperationOutcome();
      outcome.setIssue(e.getIssues());
      response.entity(ResourceComposer.compose(outcome, RequestContext.getAccept()));
      response.type(RequestContext.getAccept());
    }
    return response.build();
  }

  private static void log(FhirException e) {
    String issues = e.getIssues().stream().map(i -> i.getDetails().getText()).collect(joining(", "));
    String msg = "hello " + e.getStatusCode() + " = " + issues;
    LOG.log(e.getStatusCode() >= 500 ? Level.ERROR : Level.INFO, msg);
  }

}
