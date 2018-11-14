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
 package com.nortal.fhir.rest.root;

import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.apache.cxf.jaxrs.impl.UriBuilderImpl;
import org.apache.cxf.jaxrs.utils.HttpUtils;
import org.apache.cxf.jaxrs.utils.JAXRSUtils;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.Collections;
import java.util.List;

public class StaticUriInfo implements UriInfo {
  private final URI uri;

  public StaticUriInfo(URI uri) {
    this.uri = uri;
  }

  public StaticUriInfo(String url) {
    this(URI.create(url));
  }

  public URI getAbsolutePath() {
    return URI.create(getAbsolutePathAsString());
  }

  public UriBuilder getAbsolutePathBuilder() {
    return new UriBuilderImpl(getAbsolutePath());
  }

  public URI getBaseUri() {
    return uri;
  }

  public UriBuilder getBaseUriBuilder() {
    return new UriBuilderImpl(getBaseUri());
  }

  public String getPath() {
    return getPath(true);
  }

  public String getPath(boolean decode) {
    String value = doGetPath(decode, true);
    if (value.length() > 1 && value.startsWith("/")) {
      return value.substring(1);
    }
    return value;
  }

  public List<PathSegment> getPathSegments() {
    return getPathSegments(true);
  }

  public List<PathSegment> getPathSegments(boolean decode) {
    return JAXRSUtils.getPathSegments(getPath(false), decode);
  }

  public MultivaluedMap<String, String> getQueryParameters() {
    return getQueryParameters(true);
  }

  public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
    MetadataMap<String, String> queries = new MetadataMap<String, String>();
    JAXRSUtils.getStructuredParams(queries, uri.getQuery(), "&", decode, decode, false);
    return queries;

  }

  public URI getRequestUri() {
    String path = getAbsolutePathAsString();
    String queries = uri.getQuery();
    if (queries != null) {
      path += "?" + queries;
    }
    return URI.create(path);
  }

  public UriBuilder getRequestUriBuilder() {
    return new UriBuilderImpl(getRequestUri());
  }

  public MultivaluedMap<String, String> getPathParameters() {
    return getPathParameters(true);
  }

  public MultivaluedMap<String, String> getPathParameters(boolean decode) {
    return new MetadataMap<String, String>();
  }

  public List<Object> getMatchedResources() {
    return Collections.emptyList();
  }

  public List<String> getMatchedURIs() {
    return getMatchedURIs(true);
  }

  public List<String> getMatchedURIs(boolean decode) {
    return Collections.emptyList();
  }

  private String doGetPath(boolean decode, boolean addSlash) {
    String path = uri.getPath() + (addSlash ? "/" : "");//HttpUtils.getPathToMatch(message, addSlash);
    return decode ? HttpUtils.pathDecode(path) : path;

  }

  private String getAbsolutePathAsString() {
    String address = getBaseUri().toString();
    //    if (MessageUtils.isRequestor(message)) {
    //      return address;
    //    }
    String path = doGetPath(false, false);
    //    if (path.startsWith("/") && address.endsWith("/")) {
    //      address = address.substring(0, address.length() - 1);
    //    }
    //    if (!path.isEmpty() && !path.startsWith("/") && !address.endsWith("/")) {
    //      address = address + "/";
    //    }
    return address + path;
  }

  @Override
  public URI relativize(URI uri) {
    URI resolved = HttpUtils.resolve(getBaseUriBuilder(), uri);
    return HttpUtils.relativize(getRequestUri(), resolved);
  }

  @Override
  public URI resolve(URI uri) {
    return HttpUtils.resolve(getBaseUriBuilder(), uri);
  }
}
