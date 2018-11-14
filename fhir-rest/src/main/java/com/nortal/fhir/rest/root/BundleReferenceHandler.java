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
 package com.nortal.fhir.rest.root;

import com.nortal.blaze.core.model.VersionId;
import com.nortal.blaze.core.service.resource.ResourceService;
import com.nortal.blaze.core.util.ResourceUtil;
import com.nortal.blaze.fhir.structure.util.ResourcePropertyUtil;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryRequestComponent;
import org.hl7.fhir.dstu3.model.Bundle.HTTPVerb;
import org.hl7.fhir.dstu3.model.Reference;
import org.osgi.service.component.annotations.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Component(immediate = true, service = BundleReferenceHandler.class)
public class BundleReferenceHandler {
  @org.osgi.service.component.annotations.Reference
  private ResourceService resourceService;

  public void replaceIds(Bundle bundle) {
    // fullUrl -> local key
    Map<String, String> referenceIds = new HashMap<>();
    bundle.getEntry().forEach(entry -> {
      BundleEntryRequestComponent request = entry.getRequest();
      if (request.getMethod() == HTTPVerb.PUT) {
        VersionId id = ResourceUtil.parseReference(request.getUrl());
        referenceIds.put(entry.getFullUrl(), id.getResourceReference());
      }
      if (request.getMethod() == HTTPVerb.POST) {
        String newId = resourceService.generateNewId();
        String ref = entry.getResource().getResourceType() + "/" + newId;
        referenceIds.put(entry.getFullUrl(), ref);

        //XXX not sure if it is good idea to replace method, but how to get id before i save in this case?
        request.setUrl(ref);
        request.setMethod(HTTPVerb.PUT);
      }
    });

    ResourcePropertyUtil.findProperties(bundle, new HashSet<>(), Reference.class).forEach(reference -> {
      if (referenceIds.containsKey(reference.getReference())) {
        reference.setReference(referenceIds.get(reference.getReference()));
      }
    });
  }
}
