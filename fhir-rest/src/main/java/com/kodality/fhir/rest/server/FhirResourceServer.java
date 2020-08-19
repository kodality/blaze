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

import com.kodality.blaze.core.exception.FhirException;
import com.kodality.blaze.core.model.InteractionType;
import com.kodality.blaze.core.model.ResourceId;
import com.kodality.blaze.core.model.ResourceVersion;
import com.kodality.blaze.core.model.VersionId;
import com.kodality.blaze.core.model.search.HistorySearchCriterion;
import com.kodality.blaze.core.model.search.SearchCriterion;
import com.kodality.blaze.core.model.search.SearchResult;
import com.kodality.blaze.core.service.resource.ResourceOperationService;
import com.kodality.blaze.core.service.resource.ResourceSearchService;
import com.kodality.blaze.core.service.resource.ResourceService;
import com.kodality.blaze.core.service.resource.SearchUtil;
import com.kodality.blaze.core.util.Osgi;
import com.kodality.blaze.core.util.ResourceUtil;
import com.kodality.blaze.fhir.structure.api.FhirContentType;
import com.kodality.blaze.fhir.structure.api.ResourceComposer;
import com.kodality.blaze.fhir.structure.api.ResourceContent;
import com.kodality.fhir.rest.filter.RequestContext;
import com.kodality.fhir.rest.interaction.InteractionUtil;
import com.kodality.fhir.rest.util.BundleUtil;
import com.kodality.fhir.rest.util.PreferredReturn;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.model.UserResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestResourceComponent;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.Collections;
import java.util.List;

public class FhirResourceServer extends JaxRsServer implements FhirResourceRest {
  private static final String ETAG = "ETag";
  protected final CapabilityStatementRestResourceComponent capability;
  protected final String type;

  public FhirResourceServer(CapabilityStatementRestResourceComponent capabilityStatement) {
    this.capability = capabilityStatement;
    this.type = capabilityStatement.getType();
  }

  @Override
  protected String getEndpoint() {
    return type;
  }

  private ResourceService service() {
    return Osgi.requireBean(ResourceService.class);
  }

  private ResourceSearchService searchService() {
    return Osgi.requireBean(ResourceSearchService.class);
  }

  private ResourceOperationService operationService() {
    return Osgi.requireBean(ResourceOperationService.class);
  }

  @Override
  protected UserResource getResource() {
    UserResource resource = new UserResource(this.getClass().getName(), "/");
    resource.setConsumes(StringUtils.join(FhirContentType.getMediaTypes(), ",") + ",application/x-www-form-urlencoded");
    resource.setProduces(StringUtils.join(FhirContentType.getMediaTypes(), ","));
    resource.setOperations(InteractionUtil.getOperations(capability, FhirResourceRest.class));
    return resource;
  }

  @Override
  public Response read(String resourceId) {
    ResourceVersion version = service().load(new VersionId(type, resourceId));
    if (version.isDeleted()) {
      return Response.status(Status.GONE).header(ETAG, version.getETag()).build();
    }

    ResponseBuilder response = Response.status(Status.OK);
    response.entity(version.getContent());
    response.contentLocation(uri(version));
    response.lastModified(version.getModified());
    response.header(ETAG, version.getETag());
    return response.build();
  }

  @Override
  public Response vread(String resourceId, Integer ver) {
    ResourceVersion version = service().load(new VersionId(type, resourceId, ver));
    if (version.isDeleted()) {
      return Response.status(Status.GONE).header(ETAG, version.getETag()).build();
    }

    ResponseBuilder response = Response.status(Status.OK);
    response.entity(version.getContent());
    response.lastModified(version.getModified());
    response.header(ETAG, version.getETag());
    return response.build();
  }

  @Override
  public Response create(String body, String contentType, HttpHeaders headers) {
    String ifNoneExist = headers == null ? null : headers.getHeaderString("If-None-Exist");
    if (ifNoneExist != null) {
      ifNoneExist += "&_count=0";
      SearchCriterion criteria = new SearchCriterion(type, SearchUtil.parse(ifNoneExist, type));
      SearchResult result = searchService().search(criteria);
      if (result.getTotal() == 1) {
        return Response.status(Status.OK).build();
      }
      if (result.getTotal() > 1) {
        String er = "was expecting 0 or 1 resources. found " + result.getTotal();
        throw new FhirException(412, IssueType.PROCESSING, er);
      }
    }

    ResourceContent content = new ResourceContent(body, contentType);
    ResourceVersion version = service().save(new ResourceId(type), content, InteractionType.CREATE);
    String prefer = PreferredReturn.parse(headers);
    return created(version, prefer);
  }

  @Override
  public Response conditionalUpdate(String body, UriInfo uriInfo, String contentType, HttpHeaders headers) {
    MultivaluedMap<String, String> params = uriInfo.getQueryParameters(true);
    params.put(SearchCriterion._COUNT, Collections.singletonList("1"));
    SearchResult result = searchService().search(type, params);
    if (result.getTotal() > 1) {
      throw new FhirException(400, IssueType.PROCESSING, "was expecting 1 or 0 resources. found " + result.getTotal());
    }
    String resourceId = result.getTotal() == 1 ? result.getEntries().get(0).getId().getResourceId() : null;

    return update(resourceId, body, contentType, headers);
  }

  @Override
  public Response update(String resourceId, String body, String contentType, HttpHeaders headers) {
    String contentLocation = headers == null ? null : headers.getHeaderString("Content-Location");
    Integer ver = contentLocation == null ? null : ResourceUtil.parseReference(contentLocation).getVersion();
    ResourceContent content = new ResourceContent(body, contentType);
    ResourceVersion version = service().save(new VersionId(type, resourceId, ver), content, InteractionType.UPDATE);

    String prefer = PreferredReturn.parse(headers);
    return version.getId().getVersion() == 1 ? created(version, prefer) : updated(version, prefer);
  }

  private Response created(ResourceVersion version, String preferedReturn) {
    ResponseBuilder response = Response.status(Status.CREATED).location(uri(version));
    preferedBody(version, preferedReturn, response);
    return response.build();
  }

  private Response updated(ResourceVersion version, String preferedReturn) {
    ResponseBuilder response = Response.ok();
    response.contentLocation(uri(version));
    response.lastModified(version.getModified());
    preferedBody(version, preferedReturn, response);
    return response.build();
  }

  private void preferedBody(ResourceVersion version, String preferedReturn, ResponseBuilder response) {
    if (StringUtils.equals(preferedReturn, PreferredReturn.representation)) {
      response.entity(version.getContent());
      return;
    }
    if (StringUtils.equals(preferedReturn, PreferredReturn.OperationOutcome)) {
      OperationOutcome outcome = new OperationOutcome();
      response.entity(ResourceComposer.compose(outcome, "json"));
      return;
    }
  }

  @Override
  public Response delete(String resourceId) {
    service().delete(new ResourceId(type, resourceId));
    return Response.status(Status.NO_CONTENT).build();
  }

  @Override
  public Response history(String resourceId, UriInfo uriInfo) {
    ResourceVersion version = service().load(new VersionId(type, resourceId));
    if (version == null) {
      throw new FhirException(404, IssueType.NOTFOUND, type + "/" + resourceId + " not found");
    }
    HistorySearchCriterion criteria = new HistorySearchCriterion(type, resourceId);
    criteria.setSince(uriInfo.getQueryParameters(true).getFirst(HistorySearchCriterion._SINCE));
    List<ResourceVersion> versions = service().loadHistory(criteria);
    return Response.status(Status.OK).entity(BundleUtil.compose(null, versions, BundleType.HISTORY)).build();
  }

  @Override
  public Response historyType(UriInfo uriInfo) {
    HistorySearchCriterion criteria = new HistorySearchCriterion(type);
    criteria.setSince(uriInfo.getQueryParameters(true).getFirst(HistorySearchCriterion._SINCE));
    List<ResourceVersion> versions = service().loadHistory(criteria);
    return Response.status(Status.OK).entity(BundleUtil.compose(null, versions, BundleType.HISTORY)).build();
  }

  @Override
  public Response search_(UriInfo uriInfo) {
    return search(uriInfo);
  }

  @Override
  public Response search(UriInfo uriInfo) {
    return searchForm(uriInfo.getQueryParameters(true));
  }

  @Override
  public Response searchForm(MultivaluedMap<String, String> params) {
    //    params.keySet().forEach(key -> params.put(key, params.get(key).stream().map(v -> decode(v)).collect(toList())));
    SearchCriterion criteria = new SearchCriterion(type, SearchUtil.parse(params, type));
    SearchResult result = searchService().search(criteria);
    Bundle bundle = BundleUtil.compose(result);
    addPagingLinks(bundle, criteria.getCount(), criteria.getPage());
    return Response.status(Status.OK).entity(bundle).build();
  }

  @Override
  public Response instanceOperation(String resourceId, String operation, String body, String contentType) {
    if (!operation.startsWith("$")) {
      throw new FhirException(400, IssueType.INVALID, "operation must start with $");
    }
    ResourceId id = new ResourceId(type, resourceId);
    ResourceContent content = new ResourceContent(body, contentType);
    ResourceContent response = operationService().runInstanceOperation(operation, id, content);
    return Response.status(Status.OK).entity(response).build();
  }

  @Override
  public Response instanceOperation_(String resourceId, String operation, UriInfo uriInfo) {
    throw new FhirException(501, IssueType.NOTSUPPORTED, "GET operation not implemented");
  }

  @Override
  public Response typeOperation(String operation, String body, String contentType) {
    if (!operation.startsWith("$")) {
      throw new FhirException(400, IssueType.INVALID, "operation must start with $");
    }
    ResourceContent content = new ResourceContent(body, contentType);
    ResourceContent response = operationService().runTypeOperation(operation, type, content);
    return Response.status(Status.OK).entity(response).build();
  }

  @Override
  public Response typeOperation_(String operation, UriInfo uriInfo) {
    throw new FhirException(501, IssueType.NOTSUPPORTED, "GET operation not implemented");
  }

  private void addPagingLinks(Bundle bundle, Integer count, Integer page) {
    if (count == 0) {
      return;
    }
    // String pageUrl = RequestContext.getUriInfo().getBaseUri().toString();
    // pageUrl = StringUtils.removeEnd(pageUrl, getEndpoint());
    String pageUrl = "/" + getEndpoint();

    String queryString = RequestContext.getUriInfo().getRequestUriBuilder().build().getQuery();
    queryString = StringUtils.isEmpty(queryString) ? "" : StringUtils.removePattern(queryString, "[&?]?_page=[0-9]+");
    pageUrl += StringUtils.isEmpty(queryString) ? "?_page=" : ("?" + queryString + "&_page=");

    bundle.addLink().setRelation("self").setUrl(pageUrl + page);
    bundle.addLink().setRelation("first").setUrl(pageUrl + 1);
    bundle.addLink().setRelation("last").setUrl(pageUrl + (bundle.getTotal() / count + 1));
    if (page > 1) {
      bundle.addLink().setRelation("previous").setUrl(pageUrl + (page - 1));
    }
    if (page * count < bundle.getTotal()) {
      bundle.addLink().setRelation("next").setUrl(pageUrl + (page + 1));
    }
  }

  private URI uri(ResourceVersion version) {
    String base = RequestContext.getUriInfo().getBaseUri().toString();
    base = StringUtils.removeEnd(base, getEndpoint());
    if (version == null) {
      return URI.create(base);
    }
    return URI.create(base + version.getReference());
  }

}
