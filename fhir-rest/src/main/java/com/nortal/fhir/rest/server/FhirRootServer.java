package com.nortal.fhir.rest.server;

import com.nortal.blaze.core.exception.FhirException;
import com.nortal.blaze.core.util.Osgi;
import com.nortal.blaze.representation.ResourceComposer;
import com.nortal.blaze.representation.ResourceParser;
import com.nortal.fhir.conformance.operations.ConformanceMonitor;
import com.nortal.fhir.rest.interaction.Interaction;
import com.nortal.fhir.rest.interaction.InteractionUtil;
import com.nortal.fhir.rest.root.BatchService;
import java.util.List;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.model.UserOperation;
import org.apache.cxf.jaxrs.model.UserResource;
import org.hl7.fhir.instance.model.Bundle;
import org.hl7.fhir.instance.model.Conformance.ConformanceRestComponent;

public class FhirRootServer extends JaxRsServer implements FhirRootRest {
  private static final String TYPES = "application/xml+fhir,application/json+fhir";
  protected final ConformanceRestComponent conformance;

  public FhirRootServer(ConformanceRestComponent conformance) {
    this.conformance = conformance;
  }

  @Override
  protected String getEndpoint() {
    return ""; // root
  }

  @Override
  protected UserResource getResource() {
    UserResource resource = new UserResource(this.getClass().getName(), "/");
    resource.setConsumes(TYPES);
    resource.setProduces(TYPES);
    List<UserOperation> ops = InteractionUtil.getOperations(conformance, FhirRootRest.class);
    ops.addAll(InteractionUtil.create(Interaction.CONFORMANCE, FhirRootRest.class));
    resource.setOperations(ops);
    return resource;
  }

  @Override
  public Response conformance() {
    return Response.ok().entity(ConformanceMonitor.getConformance()).build();
  }

  @Override
  public Response conformance_() {
    return conformance();
  }

  @Override
  public Response transaction(String bundle, String contentType) {
    Bundle responseBundle = Osgi.getBean(BatchService.class).batch(ResourceParser.<Bundle> parse(bundle));
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
