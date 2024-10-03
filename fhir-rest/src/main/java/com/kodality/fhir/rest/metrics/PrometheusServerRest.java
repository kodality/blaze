package com.kodality.fhir.rest.metrics;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class PrometheusServerRest extends PrometheusServer implements PrometheusRest {

  public PrometheusServerRest() {
    super();
  }

  @Override
  public Response scrape() {
    return Response.status(Status.OK).entity(FhirMeterRegistry.getMeterRegistry().scrape()).build();
  }
}
