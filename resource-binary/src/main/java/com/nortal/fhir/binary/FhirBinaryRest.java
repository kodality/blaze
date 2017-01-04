package com.nortal.fhir.binary;

import com.nortal.fhir.rest.interaction.InteractionUtil;
import com.nortal.fhir.rest.server.FhirResourceRest;
import com.nortal.fhir.rest.server.FhirResourceServer;
import org.apache.cxf.jaxrs.model.UserResource;
import org.hl7.fhir.dstu3.model.CapabilityStatement.CapabilityStatementRestResourceComponent;

public class FhirBinaryRest extends FhirResourceServer {

  public FhirBinaryRest(CapabilityStatementRestResourceComponent capability) {
    super(capability);
  }

  @Override
  protected UserResource getResource() {
    UserResource resource = new UserResource(this.getClass().getName(), "/");
    resource.setOperations(InteractionUtil.getOperations(capability, FhirResourceRest.class));
    return resource;
  }

  // TODO: should be saved differently

}
