package com.nortal.fhir.rest.filter;

import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.message.Message;

public class ResponseFormatInterceptor implements ContainerResponseFilter {

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
      throws IOException {
    if (responseContext.getEntity() == null) {
      return;
    }
    if (RequestContext.getAccept() == null) {
      return;
    }
    for (String mime : StringUtils.split(RequestContext.getAccept(), ",")) {
      responseContext.getHeaders().putSingle(Message.CONTENT_TYPE, mime);
    }
  }

}
