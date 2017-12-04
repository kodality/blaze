package com.nortal.fhir.rest.filter.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import org.apache.cxf.message.Message;

public abstract class AbstractWriter<T> implements MessageBodyWriter<T> {

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
                      OutputStream entityStream)
      throws IOException, WebApplicationException {
    String ct = Message.CONTENT_TYPE;
    String contentType = headers.containsKey(ct) ? headers.getFirst(ct).toString() : null;
    writeTo(t, contentType, entityStream);
  }

}
