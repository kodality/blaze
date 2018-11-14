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
 package com.nortal.fhir.rest.filter.writer;

import com.nortal.blaze.fhir.structure.api.FhirContentType;
import com.nortal.blaze.fhir.structure.api.ResourceComposer;
import com.nortal.blaze.fhir.structure.api.ResourceContent;

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
