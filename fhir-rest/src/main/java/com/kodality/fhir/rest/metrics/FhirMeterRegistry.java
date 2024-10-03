package com.kodality.fhir.rest.metrics;

import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmInfoMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.DiskSpaceMetrics;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import java.io.File;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class FhirMeterRegistry {
  private static PrometheusMeterRegistry meterRegistry;

  private FhirMeterRegistry() {
  }

  public static synchronized PrometheusMeterRegistry getMeterRegistry() {
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
    new JvmThreadMetrics(tags).bindTo(meterRegistry);
    new JvmGcMetrics(tags).bindTo(meterRegistry);
    new JvmInfoMetrics().bindTo(meterRegistry);
    new ProcessorMetrics(tags).bindTo(meterRegistry);
    new FileDescriptorMetrics(tags).bindTo(meterRegistry);
    new DiskSpaceMetrics(new File("/"), tags).bindTo(meterRegistry);
  }

  private static List<Tag> getTags() {
    return List.of(new ImmutableTag("application", "blaze"));
  }
}
