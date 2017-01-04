package com.nortal.fhir.rest.filter.writer;

import com.nortal.blaze.core.model.ResourceContent;
import com.nortal.blaze.representation.api.FhirContentType;
import com.nortal.blaze.representation.api.ResourceComposer;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.Resource;

public class ResourceContentWriter extends AbstractWriter<ResourceContent> {

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return ResourceContent.class.isAssignableFrom(type);
  }

  @Override
  protected void writeTo(ResourceContent t, String contentType, OutputStream entityStream) throws IOException {
    if (contentType == null || StringUtils.equals(contentType, FhirContentType.getMimeType(t.getContentType()))) {
      entityStream.write(t.getBytes());
      return;
    }
    Resource resource = ResourceComposer.parse(t.getValue());
    entityStream.write(ResourceComposer.compose(resource, contentType).getBytes());
  }

}
