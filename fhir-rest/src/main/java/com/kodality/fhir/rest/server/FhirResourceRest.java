/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 package com.kodality.fhir.rest.server;

import com.kodality.fhir.rest.interaction.Interaction;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import static com.kodality.blaze.core.model.InteractionType.*;

public interface FhirResourceRest {

  @GET
  @Path("{id}")
  @Interaction(READ)
  Response read(@PathParam("id") String resourceId);

  @GET
  @Path("{id}/_history/{version}")
  @Interaction(VREAD)
  Response vread(@PathParam("id") String resourceId, @PathParam("version") Integer version);

  @POST
  @Interaction(CREATE)
  Response create(String body, @HeaderParam("Content-Type") String contentType, @Context HttpHeaders headers);

  @PUT
  @Path("{id}")
  @Interaction(UPDATE)
  Response update(@PathParam("id") String resourceId,
                  String body,
                  @HeaderParam("Content-Type") String contentType,
                  @Context HttpHeaders headers);

  @PUT
  @Path("")
  @Interaction(UPDATE)
  Response conditionalUpdate(String body,
                             @Context UriInfo uriInfo,
                             @HeaderParam("Content-Type") String contentType,
                             @Context HttpHeaders headers);

  @DELETE
  @Path("{id}")
  @Interaction(DELETE)
  Response delete(@PathParam("id") String resourceId);

  @GET
  @Path("{id}/_history")
  @Interaction(HISTORYINSTANCE)
  Response history(@PathParam("id") String resourceId, @Context UriInfo uriInfo);

  @GET
  @Path("_history")
  @Interaction(HISTORYTYPE)
  Response historyType(@Context UriInfo uriInfo);

  @GET
  @Interaction(SEARCHTYPE)
  Response search(@Context UriInfo uriInfo);

  @POST
  @Path("_search")
  @Interaction(SEARCHTYPE)
  Response search_(@Context UriInfo uriInfo);

  @POST
  @Path("_search")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Interaction(SEARCHTYPE)
  Response searchForm(MultivaluedMap<String, String> params);

  @POST
  @Path("{id}/{operation:\\$.+}")
  @Interaction(OPERATION)
  Response instanceOperation(@PathParam("id") String resourceId,
                             @PathParam("operation") String operation,
                             String body,
                             @HeaderParam("Content-Type") String contentType);

  @GET
  @Path("{id}/{operation:\\$.+}")
  @Interaction(OPERATION)
  Response instanceOperation_(@PathParam("id") String resourceId,
                              @PathParam("operation") String operation,
                              @Context UriInfo uriInfo);

  @POST
  @Path("{operation:\\$.+}")
  @Interaction(OPERATION)
  Response typeOperation(@PathParam("operation") String operation,
                         String body,
                         @HeaderParam("Content-Type") String contentType);

  @GET
  @Path("{operation:\\$.+}")
  @Interaction(OPERATION)
  Response typeOperation_(@PathParam("operation") String operation, @Context UriInfo uriInfo);

}
