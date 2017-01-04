package com.nortal.fhir.rest.exception;

import com.nortal.blaze.core.exception.FhirException;
import com.nortal.blaze.representation.api.ResourceComposer;
import com.nortal.fhir.rest.filter.RequestContext;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.dstu3.model.OperationOutcome.OperationOutcomeIssueComponent;

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
    if (e.getDetail() != null) {
      OperationOutcome outcome = composeOutcome(e.getDetail());
      response.entity(ResourceComposer.compose(outcome, RequestContext.getResponseMime()));
      response.type(RequestContext.getResponseMime());
    }
    return response.build();
  }

  private static void log(FhirException e) {
    String location = e.getLocation() == null ? "" : " @ " + e.getLocation();
    String msg = "hello " + e.getStatusCode() + " = " + e.getDetail() + location;
    LOG.log(e.getStatusCode() >= 500 ? Level.ERROR : Level.INFO, msg);
  }

  private static OperationOutcome composeOutcome(String detail) {
    if (detail == null) {
      return null;
    }
    CodeableConcept cc = new CodeableConcept();
    cc.addCoding().setDisplay(detail);

    OperationOutcome outcome = new OperationOutcome();
    OperationOutcomeIssueComponent issue = outcome.addIssue();
    issue.setSeverity(IssueSeverity.ERROR);
    issue.setDetails(cc);
    return outcome;
  }

  // private static String getOutcomeString(OperationOutcome outcome) {
  // if (outcome == null || outcome.getIssue() == null) {
  // return "";
  // }
  // StringBuilder sb = new StringBuilder();
  // for (OperationOutcomeIssueComponent issue : outcome.getIssue()) {
  // sb.append(issue.getDetails());
  // sb.append(getLocation(issue));
  // sb.append(";");
  // }
  // return sb.toString();
  // }
  //
  // private static String getLocation(OperationOutcomeIssueComponent issue) {
  // if (issue.getLocation() == null) {
  // return "";
  // }
  // List<String> locations = new ArrayList<>();
  // for (StringType location : issue.getLocation()) {
  // locations.add("@" + location.asStringValue());
  // }
  // return StringUtils.join(locations, ',');
  // }

}
