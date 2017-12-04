package com.nortal.fhir.conformance.operations;

import com.nortal.blaze.core.util.EtcMonitor;
import com.nortal.blaze.fhir.structure.service.ResourceRepresentationService;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.hl7.fhir.dstu3.model.CapabilityStatement;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(immediate = true)
public class CapabilityStatementMonitor extends EtcMonitor {
  private static CapabilityStatement capabilityStatement;
  @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, service = CapabilityStatementListener.class, bind = "bind", unbind = "unbind")
  private final List<CapabilityStatementListener> listeners = new ArrayList<>();
  @Reference
  private ResourceRepresentationService representationService;

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
    Resource res = representationService.parse(readFile(file));
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

  private String readFile(File file) {
    try {
      return FileUtils.readFileToString(file, "UTF8");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
