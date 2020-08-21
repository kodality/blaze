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
 package com.kodality.fhir.rest.root;

import com.kodality.blaze.core.exception.FhirException;
import com.kodality.blaze.core.exception.FhirServerException;
import com.kodality.blaze.core.util.Osgi;
import com.kodality.fhir.rest.RestResourceInitializer;
import com.kodality.fhir.rest.server.FhirResourceServer;
import com.kodality.fhir.rest.server.JaxRsServer;
import com.kodality.blaze.fhir.structure.api.ResourceComposer;
import com.kodality.blaze.fhir.structure.api.ResourceContent;
import com.kodality.blaze.fhir.structure.service.ResourceFormatService;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.EndpointException;
import org.apache.cxf.endpoint.EndpointImpl;
import org.apache.cxf.jaxrs.JAXRSServiceImpl;
import org.apache.cxf.jaxrs.impl.HttpHeadersImpl;
import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.apache.cxf.jaxrs.model.ParameterType;
import org.apache.cxf.jaxrs.utils.JAXRSUtils;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.service.model.EndpointInfo;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleEntryResponseComponent;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;
import org.hl7.fhir.r4.model.Resource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

//TODO: review or rewrite this shit
@Component(immediate = true, service = BundleEntryCxfProcessor.class)
public class BundleEntryCxfProcessor {
  @Reference
  private RestResourceInitializer restResourceInitializer;

  public BundleEntryComponent perform(BundleEntryComponent entry, String prefer) {
    String method = entry.getRequest().getMethod().toCode();
    URI uri = URI.create(entry.getRequest().getUrl().replace("|", "%7C"));
    String type = StringUtils.substringBefore(uri.getPath(), "/");
    String path = StringUtils.removeStart(uri.getPath(), type);

    Map<String, String> headers = new HashMap<>();
    if (prefer != null) {
      headers.put("Prefer", prefer);
    }
    if (entry.getRequest().getIfNoneExist() != null) {
      headers.put("If-None-Exist", entry.getRequest().getIfNoneExist());
    }
    Response response = invoke(getServer(type), method, path, uri, entry.getResource(), headers);
    return bundleEntry(response);
  }

  private Response invoke(FhirResourceServer server,
                          String method,
                          String path,
                          URI uri,
                          Resource resource,
                          Map<String, String> headers) {
    try {
      String contentType = "application/json+fhir";
      OperationResourceInfo ori = findTargetMethod(server, method, path, contentType, headers);

      Map<String, String> params = new HashMap<String, String>();
      params.putAll(headers);
      params.put(null, ResourceComposer.compose(resource, "json").getValue());
      params.put("Content-Type", contentType);
      params.put("id", StringUtils.removeStart(path, "/"));
      //      List<Object> args = JAXRSUtils.processParameters(ori, new MetadataMap<String, String>(), createDummyMessage());
      List<Object> args = ori.getParameters().stream().map(p -> {
        if (p.getType() == ParameterType.CONTEXT) {
          if (UriInfo.class.isAssignableFrom(ori.getInParameterTypes()[p.getIndex()])) {
            return new StaticUriInfo(uri);
          }
          if (HttpHeaders.class.isAssignableFrom(ori.getInParameterTypes()[p.getIndex()])) {
            MessageImpl msg = new MessageImpl();
            MultivaluedMap<String, String> msgHeaders = new MultivaluedHashMap<>();
            headers.forEach((k, v) -> msgHeaders.add(k, v));
            msg.put(Message.PROTOCOL_HEADERS, msgHeaders);
            return new HttpHeadersImpl(msg);
          }
        }
        return params.get(p.getName());
      }).collect(toList());

      Method meth = ori.getMethodToInvoke();
      Osgi.getBeans(BundleEntryCxfListener.class).forEach(l -> l.beforeInvoke(meth, uri));
      return (Response) meth.invoke(server, args.toArray());
    } catch (Exception e) {
      throw new RuntimeException(method + " " + path, e);
    }
  }

  private OperationResourceInfo findTargetMethod(FhirResourceServer server,
                                                 String method,
                                                 String path,
                                                 String contentType,
                                                 Map<String, String> headers) {
    MetadataMap<String, String> values = new MetadataMap<String, String>();
    values.putAll(headers.keySet().stream().collect(toMap(k -> k, k -> Collections.singletonList(headers.get(k)))));
    Message message = createDummyMessage();
    List<MediaType> accept = Arrays.asList(MediaType.WILDCARD_TYPE);

    JAXRSServiceImpl service = (JAXRSServiceImpl) server.getServerInstance().getEndpoint().getService();
    Map<ClassResourceInfo, MultivaluedMap<String, String>> cri =
        JAXRSUtils.selectResourceClass(service.getClassResourceInfos(), path, null);
    return JAXRSUtils.findTargetMethod(cri, message, method, values, contentType, accept, false, true);
  }

  private static Message createDummyMessage() {
    try {
      Message message = new MessageImpl();
      message.setExchange(new ExchangeImpl());
      message.getExchange().put(Endpoint.class, new EndpointImpl(null, null, new EndpointInfo()));
      return message;
    } catch (EndpointException e) {
      throw new RuntimeException(e);
    }
  }

  private BundleEntryComponent bundleEntry(Response response) {
    BundleEntryComponent newEntry = new BundleEntryComponent();
    newEntry.setResponse(entryResponse(response));
    if (response.getEntity() != null && response.getEntity() instanceof Resource) {
      newEntry.setResource((Resource) response.getEntity());
    }
    if (response.getEntity() != null && response.getEntity() instanceof ResourceContent) {
      ResourceContent content = (ResourceContent) response.getEntity();
      newEntry.setResource(Osgi.getBean(ResourceFormatService.class).parse(content.getValue()));
    }
    return newEntry;
  }

  private BundleEntryResponseComponent entryResponse(Response response) {
    BundleEntryResponseComponent entry = new BundleEntryResponseComponent();
    entry.setStatus("" + response.getStatus());
    entry.setLocation(getLocation(response));
    if (response.getStatus() >= 400) {
      entry.setOutcome(ResourceComposer.parse((String) response.getEntity()));
    }
    return entry;
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
      throw new FhirException(400, IssueType.NOTSUPPORTED, type + " not supported");
    }
    if (!(server instanceof FhirResourceServer)) {
      throw new FhirServerException(500, type + " server in invalid state");
    }
    return (FhirResourceServer) server;
  }

}
