/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 package com.kodality.fhir.rest.exception;

import com.kodality.blaze.core.exception.FhirException;
import com.kodality.blaze.fhir.structure.api.ResourceComposer;
import com.kodality.fhir.rest.filter.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hl7.fhir.r4.model.OperationOutcome;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;

import static java.util.stream.Collectors.joining;

@Slf4j
public class FhirExceptionHandler implements ExceptionMapper<Throwable> {
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

    log.error("hello", e);
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
      outcome.setExtension(e.getExtensions());
      outcome.setIssue(e.getIssues());
      outcome.setContained(e.getContained());
      String ct = RequestContext.getAccept() == null ? "application/json" : RequestContext.getAccept();
      response.entity(ResourceComposer.compose(outcome, ct));
      response.type(ct);
    }
    return response.build();
  }

  private static void log(FhirException e) {
    String issues = e.getIssues().stream().map(i -> i.getDetails().getText()).collect(joining(", "));
    String msg = "hello " + e.getStatusCode() + " = " + issues;
    if (e.getStatusCode() >= 500) {
      log.error(msg);
    } else {
      log.info(msg);
    }
  }

}
