package com.kodality.fhir.rest.metrics;

import lombok.extern.slf4j.Slf4j;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

@Slf4j
@Component(immediate = true, service = PrometheusInitializer.class)
public class PrometheusInitializer {
  @Reference
  private PrometheusServerFactory prometheusServerFactory;

  @Activate
  public void start() {
    log.info("Starting prometheus server");
    prometheusServerFactory.getServer().start();
  }

  @Deactivate
  private void stop() {
    log.info("Stopping prometheus server");
    prometheusServerFactory.getServer().stop();
  }
}
