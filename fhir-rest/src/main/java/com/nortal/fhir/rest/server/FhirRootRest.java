package com.nortal.fhir.rest.server;

import com.nortal.fhir.rest.interaction.Interaction;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

public interface FhirRootRest {

  @OPTIONS
  @Interaction(Interaction.CONFORMANCE)
  Response conformance();

  @GET
  @Path("_metadata")
  @Interaction(Interaction.CONFORMANCE)
  Response conformance_();

  @POST
  @Interaction(Interaction.TRANSACTION)
  Response transaction(String bundle, @HeaderParam("Content-Type") String contentType);

  @GET
  @Interaction(Interaction.HISTORYSYSTEM)
  Response history();

  @GET
  @Interaction(Interaction.SEARCHSYSTEM)
  Response search();
}
