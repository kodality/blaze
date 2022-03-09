package com.kodality.fhir.rest.metrics;

import io.prometheus.client.exporter.common.TextFormat;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

public interface PrometheusRest {
  @GET
  @Produces(TextFormat.CONTENT_TYPE_004)
  Response scrape();
}
