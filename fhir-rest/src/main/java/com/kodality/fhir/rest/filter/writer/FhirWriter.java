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
package com.kodality.fhir.rest.filter.writer;

import com.kodality.blaze.fhir.structure.api.ResourceComposer;
import com.kodality.blaze.fhir.structure.api.ResourceContent;
import org.hl7.fhir.r4.model.Resource;

import javax.ws.rs.core.MediaType;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public class FhirWriter extends AbstractWriter<Resource> {

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return Resource.class.isAssignableFrom(type);
  }

  @Override
  protected void writeTo(Resource t, String contentType, OutputStream entityStream) throws IOException {
    ResourceContent content = ResourceComposer.compose(t, contentType);
    entityStream.write(content.getBytes());
  }

}
