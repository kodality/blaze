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
package com.nortal.blaze.core.service.stu;

import com.nortal.blaze.core.model.InteractionType;
import com.nortal.blaze.core.model.ResourceId;
import com.nortal.blaze.core.model.VersionId;
import com.nortal.blaze.core.service.resource.ResourceService;
import com.nortal.blaze.fhir.structure.api.ResourceContent;
import com.nortal.blaze.fhir.structure.service.ResourceFormatService;
import org.hl7.fhir.r4.model.Resource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * XXX
 * some beatuful day remove structures dependency from blaze-core and move this to structures module
 * also move FhirPath.
 * and make fhir-structures dependant on fhir-core
 * 
 * ponies
 */
@Component(immediate = true, service = StuResourceService.class)
public class StuResourceService {
  @Reference
  private ResourceService resourceService;
  @Reference
  private ResourceFormatService formatService;

  public <T extends Resource> T getResource(VersionId id) {
    return formatService.parse(resourceService.load(id).getContent().getValue());
  }

  public <T extends Resource> T getResource(String reference) {
    return formatService.parse(resourceService.load(reference).getContent().getValue());
  }

  public void save(Resource r) {
    save(new ResourceId(r.getResourceType().name(), r.getId()), r);
  }

  public void save(ResourceId id, Resource r) {
    String mime = "json";
    ResourceContent json = formatService.compose(r, mime);
    String interaction = id.getResourceId() == null ? InteractionType.CREATE : InteractionType.UPDATE;
    resourceService.save(id, json, interaction);
  }

}
