package com.nortal.fhir.rest.server;

import static com.nortal.fhir.rest.interaction.Interaction.CONFORMANCE;
import static com.nortal.fhir.rest.interaction.Interaction.HISTORYSYSTEM;
import static com.nortal.fhir.rest.interaction.Interaction.SEARCHSYSTEM;
import static com.nortal.fhir.rest.interaction.Interaction.TRANSACTION;

import com.nortal.fhir.rest.interaction.Interaction;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

public interface FhirRootRest {

  @OPTIONS
  @Interaction(CONFORMANCE)
  Response conformance();

  @GET
  @Path("metadata")
  @Interaction(CONFORMANCE)
  Response conformance_();

  @POST
  @Interaction(TRANSACTION)
  Response transaction(String bundle, @HeaderParam("Content-Type") String contentType);

  @GET
  @Interaction(HISTORYSYSTEM)
  Response history();

  @GET
  @Interaction(SEARCHSYSTEM)
  Response search();
}
