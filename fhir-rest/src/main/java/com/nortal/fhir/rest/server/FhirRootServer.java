package com.nortal.fhir.rest.server;

import com.nortal.blaze.core.exception.FhirException;
import com.nortal.blaze.core.util.Osgi;
import com.nortal.blaze.fhir.structure.api.FhirContentType;
import com.nortal.blaze.fhir.structure.api.ResourceComposer;
import com.nortal.fhir.rest.RestResourceInitializer;
import com.nortal.fhir.rest.interaction.Interaction;
import com.nortal.fhir.rest.interaction.InteractionUtil;
import com.nortal.fhir.rest.root.BatchService;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.model.UserOperation;
import org.apache.cxf.jaxrs.model.UserResource;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.CapabilityStatement;
import org.hl7.fhir.dstu3.model.CapabilityStatement.CapabilityStatementRestComponent;

import javax.ws.rs.core.Response;

import java.util.List;

public class FhirRootServer extends JaxRsServer implements FhirRootRest {
  protected final CapabilityStatementRestComponent capabilityStatement;

  public FhirRootServer(CapabilityStatementRestComponent capabilityStatement) {
    this.capabilityStatement = capabilityStatement;
  }

  @Override
  protected String getEndpoint() {
    return ""; // root
  }

  @Override
  protected UserResource getResource() {
    UserResource resource = new UserResource(this.getClass().getName(), "/");
    resource.setConsumes(StringUtils.join(FhirContentType.getMediaTypes(), ","));
    resource.setProduces(StringUtils.join(FhirContentType.getMediaTypes(), ","));
    List<UserOperation> ops = InteractionUtil.getOperations(capabilityStatement, FhirRootRest.class);
    ops.addAll(InteractionUtil.create(Interaction.CONFORMANCE, FhirRootRest.class));
    resource.setOperations(ops);
    return resource;
  }

  @Override
  public Response conformance() {
    CapabilityStatement capability = Osgi.getBean(RestResourceInitializer.class).getModifiedCapability();
    return Response.ok().entity(capability).build();
  }

  @Override
  public Response conformance_() {
    return conformance();
  }

  @Override
  public Response transaction(String bundle, String contentType) {
    if (StringUtils.isEmpty(bundle)) {
      return Response.status(204).build();
    }
    Bundle responseBundle = Osgi.getBean(BatchService.class).batch(ResourceComposer.<Bundle>parse(bundle));
    return Response.ok().entity(ResourceComposer.compose(responseBundle, contentType)).build();
  }

  @Override
  public Response history() {
    throw new FhirException(501, "well sorry");
  }

  @Override
  public Response search() {
    throw new FhirException(501, "well sorry");
  }

}
