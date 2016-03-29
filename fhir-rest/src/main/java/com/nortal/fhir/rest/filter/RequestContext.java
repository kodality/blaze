package com.nortal.fhir.rest.filter;

import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.apache.log4j.Logger;

public class RequestContext implements ContainerRequestFilter {
  private static final ThreadLocal<UriInfo> uriInfo = new ThreadLocal<>();
  private static final ThreadLocal<String> responseMime = new ThreadLocal<>();

  public static UriInfo getUriInfo() {
    return uriInfo.get();
  }

  public static String getResponseMime() {
    return responseMime.get();
  }

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    try {
      uriInfo.set(requestContext.getUriInfo());
      String accept = requestContext.getHeaderString("Accept");
      String contentType = requestContext.getHeaderString("Content-Type");
      responseMime.set(accept == null || accept.equals(MediaType.WILDCARD) ? contentType : accept);
    } catch (Exception e) {
      Logger.getLogger(RequestContext.class).error(e);
    }
  }

  public static void clear() {
    try {
      uriInfo.remove();
      responseMime.remove();
    } catch (Exception e) {
      Logger.getLogger(RequestContext.class).error(e);
    }
  }

}
