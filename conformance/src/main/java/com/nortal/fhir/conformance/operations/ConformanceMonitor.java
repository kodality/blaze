package com.nortal.fhir.conformance.operations;

import com.nortal.blaze.core.util.EtcMonitor;
import com.nortal.blaze.representation.ResourceParser;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.hl7.fhir.instance.model.Conformance;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;

@Component(immediate = true)
public class ConformanceMonitor extends EtcMonitor {
  private static Conformance conformance;

  @Reference(cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, referenceInterface = ConformanceListener.class, policy = ReferencePolicy.DYNAMIC)
  private final List<ConformanceListener> listeners = new ArrayList<>();

  public ConformanceMonitor() {
    super("conformance");
  }

  @Activate
  private void init() {
    start();
  }

  @Deactivate
  private void destroy() {
    stop();
  }

  public static Conformance getConformance() {
    return conformance;
  }

  @Override
  protected void clear() {
    conformance = null;
  }

  @Override
  protected void file(File file) {
    Resource res = ResourceParser.parse(file);
    if (ResourceType.Conformance != res.getResourceType()) {
      return;
    }
    conformance = (Conformance) res;
    listeners.forEach(l -> l.comply(conformance));
  }

  protected void bind(ConformanceListener listener) {
    listeners.add(listener);
  }

  protected void unbind(ConformanceListener listener) {
    listeners.remove(listener);
  }

}
