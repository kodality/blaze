package com.kodality.fhir.rest.metrics;

import org.osgi.service.component.annotations.Component;

@Component(immediate = true, service = PrometheusServerFactory.class)
public class PrometheusServerFactory {
  private PrometheusServer server;

  public PrometheusServer getServer() {
    if (server == null) {
      server = new PrometheusServerRest();
    }
    return server;
  }
}
