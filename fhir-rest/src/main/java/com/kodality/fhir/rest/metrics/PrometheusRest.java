package com.kodality.fhir.rest.metrics;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

public interface PrometheusRest {
  @GET
  @Produces("text/plain; version=0.0.4")
  Response scrape();
}
