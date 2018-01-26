package com.nortal.fhir.rest.server;

import com.nortal.blaze.core.model.ResourceContent;
import com.nortal.blaze.core.model.ResourceId;
import com.nortal.blaze.core.model.ResourceVersion;
import com.nortal.blaze.core.model.VersionId;
import com.nortal.blaze.core.model.search.SearchCriterion;
import com.nortal.blaze.core.model.search.SearchResult;
import com.nortal.blaze.core.service.ResourceService;
import com.nortal.blaze.core.util.Osgi;
import com.nortal.blaze.core.util.ResourceUtil;
import com.nortal.blaze.fhir.structure.api.FhirContentType;
import com.nortal.blaze.fhir.structure.api.ResourceComposer;
import com.nortal.fhir.rest.filter.RequestContext;
import com.nortal.fhir.rest.interaction.InteractionUtil;
import com.nortal.fhir.rest.util.SearchUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.model.UserResource;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleType;
import org.hl7.fhir.dstu3.model.CapabilityStatement.CapabilityStatementRestResourceComponent;
import org.hl7.fhir.dstu3.model.Resource;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import java.net.URI;
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

  @Override
  protected UserResource getResource() {
    UserResource resource = new UserResource(this.getClass().getName(), "/");
    resource.setConsumes(StringUtils.join(FhirContentType.getMediaTypes(), ","));
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

    ResponseBuilder response = Response.status(Status.OK);
    response.entity(version.getContent());
    response.lastModified(version.getModified());
    response.header(ETAG, version.getETag());
    return response.build();
  }

  @Override
  public Response create(String body, String contentType) {
    ResourceContent content = new ResourceContent(body, contentType);
    ResourceVersion version = service().save(new VersionId(type), content);
    return Response.status(Status.CREATED).location(uri(version)).build();
  }

  @Override
  public Response update(String resourceId, String body, String contentType, String contentLocation) {
    Integer ver = contentLocation == null ? null : ResourceUtil.parseReference(contentLocation).getVersion();
    ResourceContent content = new ResourceContent(body, contentType);
    ResourceVersion version = service().save(new VersionId(type, resourceId, ver), content);

    ResponseBuilder response = Response.status(Status.OK);
    response.contentLocation(uri(version));
    response.lastModified(version.getModified());
    return response.build();
  }

  @Override
  public Response delete(String resourceId) {
    service().delete(new ResourceId(type, resourceId));
    return Response.status(Status.NO_CONTENT).build();
  }

  @Override
  public Response history(String resourceId) {
    List<ResourceVersion> versions = service().loadHistory(new ResourceId(type, resourceId));
    return Response.status(Status.OK).entity(compose(null, versions, BundleType.HISTORY)).build();
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
    params.remove("");
    params.remove(null);// will this is strange
    SearchCriterion criteria = new SearchCriterion();
    criteria.setType(type);
    criteria.setParams(SearchUtil.parse(params, type));
    SearchResult result = service().search(criteria);
    Bundle bundle = compose(result.getTotal(), result.getEntries(), BundleType.SEARCHSET);
    addPagingLinks(bundle, criteria.getCount(), criteria.getPage());
    return Response.status(Status.OK).entity(bundle).build();
  }

  public static Bundle compose(Integer total, List<ResourceVersion> versions, BundleType bundleType) {
    Bundle bundle = new Bundle();
    bundle.setTotal(total == null ? versions.size() : total);
    bundle.setType(bundleType);
    for (ResourceVersion version : versions) {
      if (version.getContent() != null && version.getContent().getValue() != null) {
        Resource resource = ResourceComposer.parse(version.getContent().getValue());
        bundle.addEntry().setResource(resource);// .setId(version.getId().getResourceId());
      }
    }
    return bundle;
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
