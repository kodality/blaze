package com.nortal.fhir.conformance;

import com.nortal.blaze.core.service.conformance.ConformanceHolder;
import com.nortal.blaze.fhir.structure.service.ResourceRepresentationService;
import org.apache.commons.io.FileUtils;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.CapabilityStatement;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.io.File;
import java.io.IOException;

@Component(immediate = true)
public class CapabilityStatementMonitor extends EtcMonitor {
  @Reference
  private ResourceRepresentationService representationService;
  @Reference
  private ConformanceHolder conformanceHolder;

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

  @Override
  protected void clear() {
    //
  }

  @Override
  protected void file(File file) {
    Resource res = representationService.parse(readFile(file));
    if (ResourceType.CapabilityStatement == res.getResourceType()) {
      conformanceHolder.setCapabilityStatement((CapabilityStatement) res);
      return;
    }
    if (ResourceType.Bundle == res.getResourceType()) {
      Bundle bundle = (Bundle) res;
      BundleEntryComponent entry = bundle.getEntry().get(0);
      if (ResourceType.CapabilityStatement == entry.getResource().getResourceType()) {
        conformanceHolder.setCapabilityStatement((CapabilityStatement) entry.getResource());
      }
    }
  }

  private String readFile(File file) {
    try {
      return FileUtils.readFileToString(file, "UTF8");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
