package com.nortal.fhir.rest.server;

import com.nortal.fhir.rest.interaction.Interaction;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public interface FhirResourceRest {

  @GET
  @Path("{id}")
  @Interaction(Interaction.READ)
  Response read(@PathParam("id") String resourceId);

  @GET
  @Path("{id}/_history/{version}")
  @Interaction(Interaction.VREAD)
  Response vread(@PathParam("id") String resourceId, @PathParam("version") Integer version);

  @POST
  @Interaction(Interaction.CREATE)
  Response create(String body, @HeaderParam("Content-Type") String contentType);

  @PUT
  @Path("{id}")
  @Interaction(Interaction.UPDATE)
  Response update(@PathParam("id") String resourceId,
                  String body,
                  @HeaderParam("Content-Type") String contentType,
                  @HeaderParam("Content-Location") String contentLocation);

  @DELETE
  @Path("{id}")
  @Interaction(Interaction.DELETE)
  Response delete(@PathParam("id") String resourceId);

  @GET
  @Path("{id}/_history")
  @Interaction(Interaction.HISTORYINSTANCE)
  Response history(@PathParam("id") String resourceId);

  @GET
  @Interaction(Interaction.SEARCHTYPE)
  Response search(@Context UriInfo uriInfo);

  @POST
  @Path("_search")
  @Interaction(Interaction.SEARCHTYPE)
  Response search_(@Context UriInfo uriInfo);

  @POST
  @Path("_search")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Interaction(Interaction.SEARCHTYPE)
  Response searchForm(MultivaluedMap<String, String> params);

}
