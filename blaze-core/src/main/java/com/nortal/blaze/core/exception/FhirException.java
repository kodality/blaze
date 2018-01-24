package com.nortal.blaze.core.exception;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.dstu3.model.OperationOutcome.OperationOutcomeIssueComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FhirException extends RuntimeException {
  private final int statusCode;
  private final List<OperationOutcomeIssueComponent> issues;

  public FhirException(int statusCode) {
    this(statusCode, new ArrayList<>(0));
  }

  public FhirException(int statusCode, List<OperationOutcomeIssueComponent> issues) {
    this.statusCode = statusCode;
    this.issues = issues;
  }

  public FhirException(Throwable cause) {
    this(500, cause);
  }

  public FhirException(int statusCode, Throwable cause) {
    super(cause);
    this.statusCode = statusCode;
    this.issues = Collections.singletonList(composeIssue(cause.getMessage()));
  }

  public FhirException(int statusCode, String detail) {
    this.statusCode = statusCode;
    this.issues = Collections.singletonList(composeIssue(detail));
  }

  public int getStatusCode() {
    return statusCode;
  }

  public List<OperationOutcomeIssueComponent> getIssues() {
    return issues;
  }

  private static OperationOutcomeIssueComponent composeIssue(String detail) {
    CodeableConcept cc = new CodeableConcept();
    cc.addCoding().setDisplay(detail);

    OperationOutcomeIssueComponent issue = new OperationOutcomeIssueComponent();
    issue.setSeverity(IssueSeverity.ERROR);
    issue.setDetails(cc);
    return issue;
  }

}
