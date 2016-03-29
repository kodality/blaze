package com.nortal.fhir.binary;

import com.nortal.fhir.rest.interaction.InteractionUtil;
import com.nortal.fhir.rest.server.FhirResourceRest;
import com.nortal.fhir.rest.server.FhirResourceServer;
import org.apache.cxf.jaxrs.model.UserResource;
import org.hl7.fhir.instance.model.Conformance.ConformanceRestResourceComponent;

public class FhirBinaryRest extends FhirResourceServer {

  public FhirBinaryRest(ConformanceRestResourceComponent conformance) {
    super(conformance);
  }

  @Override
  protected UserResource getResource() {
    UserResource resource = new UserResource(this.getClass().getName(), "/");
    resource.setOperations(InteractionUtil.getOperations(conformance, FhirResourceRest.class));
    return resource;
  }

  // TODO: should be saved differently

}
