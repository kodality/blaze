package com.nortal.fhir.rest.filter;

import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.apache.logging.log4j.LogManager;

public class RequestContext implements ContainerRequestFilter {
  private static final ThreadLocal<UriInfo> uriInfo = new ThreadLocal<>();
  private static final ThreadLocal<String> responseMime = new ThreadLocal<>();
  private static final String DEFAULT = "application/json+fhir";

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
      String contentType = requestContext.getHeaderString("Content-Type");
      String accept = requestContext.getHeaderString("Accept");
      accept =
          accept == null || accept.equals(MediaType.WILDCARD) ? contentType == null ? DEFAULT : contentType : accept;
      responseMime.set(accept);
    } catch (Exception e) {
      LogManager.getLogger(RequestContext.class).error(e);
    }
  }

  public static void clear() {
    try {
      uriInfo.remove();
      responseMime.remove();
    } catch (Exception e) {
      LogManager.getLogger(RequestContext.class).error(e);
    }
  }

}
