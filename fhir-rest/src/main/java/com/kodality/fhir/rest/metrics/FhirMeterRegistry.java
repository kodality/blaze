package com.kodality.fhir.rest.metrics;

import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import java.util.List;

public class FhirMeterRegistry {
  private static PrometheusMeterRegistry meterRegistry;

  public static PrometheusMeterRegistry getMeterRegistry() {
    if (meterRegistry == null) {
      initMeterRegistry();
    }
    return meterRegistry;
  }

  private static void initMeterRegistry() {
    meterRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    List<Tag> tags = getTags();
    meterRegistry.config().commonTags(tags);
    new ClassLoaderMetrics(tags).bindTo(meterRegistry);
    new JvmMemoryMetrics(tags).bindTo(meterRegistry);
    new JvmGcMetrics(tags).bindTo(meterRegistry);
    new ProcessorMetrics(tags).bindTo(meterRegistry);
    new JvmThreadMetrics(tags).bindTo(meterRegistry);
  }

  private static List<Tag> getTags() {
    return List.of(new ImmutableTag("application", "blaze"));
  }

}
