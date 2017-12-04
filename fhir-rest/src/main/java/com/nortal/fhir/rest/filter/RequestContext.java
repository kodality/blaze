package com.nortal.fhir.rest.filter;

import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.apache.logging.log4j.LogManager;

public class RequestContext implements ContainerRequestFilter {
  private static final ThreadLocal<UriInfo> uriInfo = new ThreadLocal<>();
  private static final ThreadLocal<String> accept = new ThreadLocal<>();
  private static final String DEFAULT = "application/json";

  public static UriInfo getUriInfo() {
    return uriInfo.get();
  }

  public static String getAccept() {
    return accept.get();
  }

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    try {
      uriInfo.set(requestContext.getUriInfo());
      accept.set(readAccept(requestContext));
    } catch (Exception e) {
      LogManager.getLogger(RequestContext.class).error(e);
    }
  }

  private String readAccept(ContainerRequestContext requestContext) {
    String result = requestContext.getHeaderString("Accept");
    if (result == null || result.equals(MediaType.WILDCARD)) {
      String contentType = requestContext.getHeaderString("Content-Type");
      return contentType == null ? DEFAULT : contentType;
    }
    return result;
  }

  public static void clear() {
    try {
      uriInfo.remove();
      accept.remove();
    } catch (Exception e) {
      LogManager.getLogger(RequestContext.class).error(e);
    }
  }

}
