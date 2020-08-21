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
 package com.kodality.blaze.fhir.structure.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(immediate = true, service = FhirContentType.class)
public class FhirContentType {
  private static final Map<String, String> mimes = new HashMap<>();
  private static final List<String> mediaTypes = new ArrayList<>();

  public static List<String> getMediaTypes() {
    return mediaTypes;
  }

  public static String getMimeType(String type) {
    return mimes.get(type);
  }

  @Reference(name = "presenters", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, unbind = "unbind")
  protected void bind(ResourceRepresentation presenter) {
    String main = presenter.getMimeTypes().get(0);
    presenter.getMimeTypes().forEach(mime -> {
      if (mimes.containsKey(mime)) {
        throw new IllegalStateException(" multiple composers for mime " + mime);
      }
      mimes.put(mime, main);
      if (mime.contains("/")) {
        mediaTypes.add(mime);
      }
    });
  }

  protected void unbind(ResourceRepresentation presenter) {
    presenter.getMimeTypes().forEach(mime -> {
      mimes.remove(mime);
      mediaTypes.remove(mime);
    });
  }

}
