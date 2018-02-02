package com.nortal.fhir.rest.root;

import com.nortal.blaze.core.exception.FhirException;
import com.nortal.blaze.fhir.structure.api.ResourceComposer;
import com.nortal.fhir.rest.RestResourceInitializer;
import com.nortal.fhir.rest.server.FhirResourceServer;
import com.nortal.fhir.rest.server.JaxRsServer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.EndpointException;
import org.apache.cxf.endpoint.EndpointImpl;
import org.apache.cxf.jaxrs.JAXRSServiceImpl;
import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.apache.cxf.jaxrs.utils.JAXRSUtils;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.service.model.EndpointInfo;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryRequestComponent;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryResponseComponent;
import org.hl7.fhir.dstu3.model.Bundle.HTTPVerb;
import org.hl7.fhir.dstu3.model.Resource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

//TODO: review or rewrite this shit
@Component(immediate = true, service = BundleEntryCxfProcessor.class)
public class BundleEntryCxfProcessor {
  @Reference
  private RestResourceInitializer restResourceInitializer;

  public BundleEntryResponseComponent perform(BundleEntryComponent entry) {
    Validate.isTrue(entry.hasRequest() || entry.hasResource());
    if (!entry.hasRequest()) {
      entry.setRequest(new BundleEntryRequestComponent());
      if (entry.getResource().hasId()) {
        entry.getRequest().setMethod(HTTPVerb.PUT);
        entry.getRequest().setUrl(entry.getResource().getResourceType().name() + "/" + entry.getResource().getId());
      } else {
        entry.getRequest().setMethod(HTTPVerb.POST);
        entry.getRequest().setUrl(entry.getResource().getResourceType().name());
      }
    }

    String method = entry.getRequest().getMethod().toCode();
    URI url = URI.create(entry.getRequest().getUrl());
    String type = StringUtils.substringBefore(url.getPath(), "/");
    String path = StringUtils.removeStart(url.getPath(), type);

    Response response = invoke(getServer(type), method, path, entry.getResource());
    return bundleResponse(response);
  }

  private Response invoke(FhirResourceServer server, String method, String path, Resource resource) {
    try {
      String contentType = "application/json+fhir";
      OperationResourceInfo ori = findTargetMethod(server, method, path, contentType);

      Map<String, String> params = new HashMap<String, String>();
      params.put(null, ResourceComposer.compose(resource, "json"));
      params.put("Content-Type", contentType);
      params.put("id", StringUtils.removeStart(path, "/"));
      List<Object> args = ori.getParameters().stream().map(p -> params.get(p.getName())).collect(toList());

      return (Response) ori.getMethodToInvoke().invoke(server, args.toArray());
    } catch (Exception e) {
      throw new RuntimeException(method + " " + path, e);
    }
  }

  private OperationResourceInfo findTargetMethod(FhirResourceServer server,
                                                 String method,
                                                 String path,
                                                 String contentType)
      throws EndpointException {
    MetadataMap<String, String> values = new MetadataMap<String, String>();
    Message message = createDummyMessage();
    List<MediaType> accept = Arrays.asList(MediaType.WILDCARD_TYPE);

    JAXRSServiceImpl service = (JAXRSServiceImpl) server.getServerInstance().getEndpoint().getService();
    Map<ClassResourceInfo, MultivaluedMap<String, String>> cri =
        JAXRSUtils.selectResourceClass(service.getClassResourceInfos(), path, null);
    return JAXRSUtils.findTargetMethod(cri, message, method, values, contentType, accept, false, true);
  }

  private Message createDummyMessage() throws EndpointException {
    Message message = new MessageImpl();
    message.setExchange(new ExchangeImpl());
    message.getExchange().put(Endpoint.class, new EndpointImpl(null, null, new EndpointInfo()));
    return message;
  }

  private BundleEntryResponseComponent bundleResponse(Response response) {
    BundleEntryResponseComponent bundle = new BundleEntryResponseComponent();
    bundle.setStatus("" + response.getStatus());
    bundle.setLocation(getLocation(response));
    if (response.getStatus() >= 400) {
      bundle.setOutcome(ResourceComposer.parse((String) response.getEntity()));
    }
    return bundle;
  }

  private String getLocation(Response response) {
    if (response.getMetadata().containsKey("Content-Location")) {
      return ((URI) response.getMetadata().get("Content-Location").get(0)).toString();
    }
    if (response.getMetadata().containsKey("Location")) {
      return ((URI) response.getMetadata().get("Location").get(0)).toString();
    }
    return null;
  }

  private FhirResourceServer getServer(String type) {
    JaxRsServer server = restResourceInitializer.getServers().get(type);
    if (server == null) {
      throw new FhirException(400, type + " not supported");
    }
    if (!(server instanceof FhirResourceServer)) {
      throw new FhirException(500, type + " not supported");
    }
    return (FhirResourceServer) server;
  }
}
