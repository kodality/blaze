package com.kodality.blaze.fhir.structure.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import org.hl7.fhir.r4.context.IWorkerContext;
import org.hl7.fhir.r4.hapi.ctx.HapiWorkerContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(immediate = true, service = HapiContextHolder.class)
public class HapiContextHolder {
  private IWorkerContext hapiContext;

  public IWorkerContext getContext() {
    return hapiContext;
  }

  @Activate
  private void init() {
    FhirContext r4 = FhirContext.forR4();
    hapiContext = new HapiWorkerContext(r4, new DefaultProfileValidationSupport(r4));
  }
}
