package com.nortal.fhir.rest.filter.writer;

import com.nortal.blaze.fhir.structure.api.ResourceComposer;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.core.MediaType;
import org.hl7.fhir.dstu3.model.Resource;

public class FhirWriter extends AbstractWriter<Resource> {

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return Resource.class.isAssignableFrom(type);
  }

  @Override
  protected void writeTo(Resource t, String contentType, OutputStream entityStream) throws IOException {
    String content = ResourceComposer.compose(t, contentType);
    entityStream.write(content.getBytes("UTF-8"));
  }

}
