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
 package com.kodality.blaze.blockchain;

import com.kodality.blaze.core.api.resource.InstanceOperationDefinition;
import com.kodality.blaze.core.model.ResourceId;
import com.kodality.blaze.core.model.ResourceVersion;
import com.kodality.blaze.core.service.resource.ResourceService;
import com.kodality.blaze.fhir.structure.api.ResourceContent;
import com.kodality.blaze.fhir.structure.service.ResourceFormatService;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.StringType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(immediate = true, service = InstanceOperationDefinition.class)
public class DocumentProofCheckOperation implements InstanceOperationDefinition {

  @Reference
  private DocumentNotary notary;

  @Reference
  private ResourceService resourceService;

  @Reference
  private ResourceFormatService formatService;

  @Override
  public String getResourceType() {
    return ResourceType.Patient.name();
  }

  @Override
  public String getOperationName() {
    return "proof-check";
  }

  @Override
  public ResourceContent run(ResourceId id, ResourceContent parameters) {
    ResourceVersion version = resourceService.load(id.getReference());
    String documentUnchanged = notary.checkDocument(version);
    Parameters parameter = new Parameters();
    parameter.addParameter().setName("valid").setValue(new StringType().setValue(documentUnchanged));
    return formatService.compose(parameter, "json");
  }
}
