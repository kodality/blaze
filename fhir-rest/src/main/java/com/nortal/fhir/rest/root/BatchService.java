package com.nortal.fhir.rest.root;

import com.nortal.blaze.core.exception.FhirException;
import com.nortal.blaze.representation.api.ResourceComposer;
import com.nortal.fhir.rest.RestResourceInitializer;
import com.nortal.fhir.rest.exception.FhirExceptionHandler;
import com.nortal.fhir.rest.server.FhirResourceServer;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.EndpointException;
import org.apache.cxf.endpoint.EndpointImpl;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServiceImpl;
import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.apache.cxf.jaxrs.model.Parameter;
import org.apache.cxf.jaxrs.utils.JAXRSUtils;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.felix.scr.annotations.Component;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryRequestComponent;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryResponseComponent;
import org.hl7.fhir.dstu3.model.Bundle.BundleType;
import org.hl7.fhir.dstu3.model.Bundle.HTTPVerb;
import org.hl7.fhir.dstu3.model.Resource;

// TODO: review or rewrite this shit
@Component(immediate = true)
public class BatchService {
  @javax.annotation.Resource
  private RestResourceInitializer restResourceInitializer;

  public Bundle batch(Bundle bundle) {
    BundleType type = bundle.getType();
    Validate.isTrue(type == BundleType.BATCH || type == BundleType.TRANSACTION);

    Bundle responseBundle = new Bundle();
    responseBundle.setType(BundleType.TRANSACTIONRESPONSE);
    for (BundleEntryComponent entry : bundle.getEntry()) {
      BundleEntryResponseComponent response = perform(entry.getRequest(), entry.getResource());
      responseBundle.addEntry().setResponse(response);
    }
    return responseBundle;
  }

  private BundleEntryResponseComponent perform(BundleEntryRequestComponent request, Resource resource) {
    Validate.isTrue(request != null || resource != null);
    if (request == null) {
      request = new BundleEntryRequestComponent();
      request.setMethod(HTTPVerb.POST);
      request.setUrl(resource.getResourceType().name());
    }

    String method = request.getMethod().toCode();
    URI url = URI.create(request.getUrl());
    String type = StringUtils.substringBefore(url.getPath(), "/");
    String path = StringUtils.removeStart(url.getPath(), type);

    JAXRSServiceImpl service = getService(type);
    Response response = invoke(service, method, path, resource);
    return bundleResponse(response);
  }

  private Response invoke(JAXRSServiceImpl service, String method, String path, Resource resource) {
    try {
      MetadataMap<String, String> values = new MetadataMap<String, String>();
      Message message = createDummyMessage();
      List<MediaType> accept = Arrays.asList(MediaType.WILDCARD_TYPE);
      String contentType = "application/json+fhir";
      Map<String, String> params = new HashMap<String, String>();
      String json = ResourceComposer.compose(resource, "json");
      params.put(null, json);
      params.put("Content-Type", contentType);

      Map<ClassResourceInfo, MultivaluedMap<String, String>> cri =
          JAXRSUtils.selectResourceClass(service.getClassResourceInfos(), path, null);
      OperationResourceInfo ori = JAXRSUtils.findTargetMethod(cri, message, method, values, contentType, accept, false);

      FhirResourceServer server =
          (FhirResourceServer) cri.keySet().iterator().next().getResourceProvider().getInstance(message);
      Object[] args = getArgs(ori.getParameters(), params).toArray();
      return (Response) ori.getMethodToInvoke().invoke(server, args);

    } catch (InvocationTargetException e) {
      if (e.getCause() instanceof FhirException) {
        FhirException fhirException = (FhirException) e.getCause();
        if (fhirException.getStatusCode() < 500) {
          return FhirExceptionHandler.getResponse(fhirException);
        }
      }
      throw new RuntimeException(method + " " + path, e.getCause());
    } catch (Exception e) {
      throw new RuntimeException(method + " " + path, e);
    }
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

  private List<Object> getArgs(List<Parameter> methodParameters, Map<String, String> params) {
    List<Object> args = new ArrayList<>();
    for (Parameter parameter : methodParameters) {
      args.add(params.get(parameter.getName()));
    }
    return args;
  }

  private JAXRSServiceImpl getService(String type) {
    Server server = restResourceInitializer.getServers().get(type);
    if (server == null) {
      throw new FhirException(400, type + " not supported");
    }
    return (JAXRSServiceImpl) server.getEndpoint().getService();
  }

}
