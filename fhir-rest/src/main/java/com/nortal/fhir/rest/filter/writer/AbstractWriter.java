package com.nortal.fhir.rest.filter.writer;

import com.nortal.fhir.rest.filter.RequestContext;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.message.Message;

public abstract class AbstractWriter<T> implements MessageBodyWriter<T>, ContainerResponseFilter {

  protected abstract void writeTo(T t, String contentType, OutputStream entityStream) throws IOException;

  @Override
  public long getSize(T t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return -1;// funny javadoc
  }

  @Override
  public void writeTo(T t,
                      Class<?> type,
                      Type genericType,
                      Annotation[] annotations,
                      MediaType mediaType,
                      MultivaluedMap<String, Object> headers,
                      OutputStream entityStream) throws IOException, WebApplicationException {
    String contentType =
        headers.containsKey(Message.CONTENT_TYPE) ? headers.getFirst(Message.CONTENT_TYPE).toString() : null;
    writeTo(t, contentType, entityStream);
  }

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
      throws IOException {
    if (responseContext.getEntity() == null) {
      return;
    }
    if(RequestContext.getResponseMime() == null){
      return;
    }
    for (String mime : StringUtils.split(RequestContext.getResponseMime(), ",")) {
      if (isWriteable(responseContext.getEntity().getClass(),
                      responseContext.getEntityType(),
                      responseContext.getEntityAnnotations(),
                      MediaType.valueOf(mime))) {
        responseContext.getHeaders().putSingle(Message.CONTENT_TYPE, mime);
        return;
      }
    }
  }

}
