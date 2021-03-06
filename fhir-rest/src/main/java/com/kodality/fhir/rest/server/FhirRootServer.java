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

import com.kodality.blaze.core.exception.FhirServerException;
import com.kodality.blaze.core.model.InteractionType;
import com.kodality.blaze.core.model.ResourceVersion;
import com.kodality.blaze.core.model.search.HistorySearchCriterion;
import com.kodality.blaze.core.service.resource.ResourceService;
import com.kodality.blaze.core.util.Osgi;
import com.kodality.fhir.rest.RestResourceInitializer;
import com.kodality.fhir.rest.root.BundleService;
import com.kodality.blaze.fhir.structure.api.FhirContentType;
import com.kodality.blaze.fhir.structure.api.ResourceComposer;
import com.kodality.fhir.rest.interaction.InteractionUtil;
import com.kodality.fhir.rest.util.BundleUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.model.UserOperation;
import org.apache.cxf.jaxrs.model.UserResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestComponent;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

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
    ops.addAll(InteractionUtil.create(InteractionType.CONFORMANCE, FhirRootRest.class));
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
  public Response transaction(String bundle, String contentType, HttpHeaders headers) {
    if (StringUtils.isEmpty(bundle)) {
      return Response.status(204).build();
    }
    String prefer = headers.getHeaderString("Prefer");
    Bundle responseBundle = Osgi.getBean(BundleService.class).save(ResourceComposer.<Bundle> parse(bundle), prefer);
    return Response.ok().entity(ResourceComposer.compose(responseBundle, contentType)).build();
  }

  @Override
  public Response history(UriInfo uriInfo) {
    HistorySearchCriterion criteria = new HistorySearchCriterion();
    criteria.setSince(uriInfo.getQueryParameters(true).getFirst(HistorySearchCriterion._SINCE));
    List<ResourceVersion> versions = Osgi.getBean(ResourceService.class).loadHistory(criteria);
    return Response.status(Status.OK).entity(BundleUtil.compose(null, versions, BundleType.HISTORY)).build();
  }

  @Override
  public Response search() {
    throw new FhirServerException(501, "system search not implemented");
  }

}
