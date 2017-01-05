package com.nortal.fhir.conformance.operations;

import com.nortal.blaze.core.util.EtcMonitor;
import com.nortal.blaze.fhir.structure.api.ResourceComposer;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.hl7.fhir.dstu3.model.CapabilityStatement;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.ResourceType;

@Component(immediate = true)
public class CapabilityStatementMonitor extends EtcMonitor {
  private static CapabilityStatement capabilityStatement;

  @Reference(cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, referenceInterface = CapabilityStatementListener.class, policy = ReferencePolicy.DYNAMIC)
  private final List<CapabilityStatementListener> listeners = new ArrayList<>();

  public CapabilityStatementMonitor() {
    super("capability");
  }

  @Activate
  private void init() {
    start();
  }

  @Deactivate
  private void destroy() {
    stop();
  }

  public static CapabilityStatement getCapabilityStatement() {
    return capabilityStatement;
  }

  @Override
  protected void clear() {
    capabilityStatement = null;
  }

  @Override
  protected void file(File file) {
    Resource res = ResourceComposer.parse(file);
    if (ResourceType.CapabilityStatement != res.getResourceType()) {
      return;
    }
    capabilityStatement = (CapabilityStatement) res;
    listeners.forEach(l -> l.comply(capabilityStatement));
  }

  protected void bind(CapabilityStatementListener listener) {
    listeners.add(listener);
  }

  protected void unbind(CapabilityStatementListener listener) {
    listeners.remove(listener);
  }

}
