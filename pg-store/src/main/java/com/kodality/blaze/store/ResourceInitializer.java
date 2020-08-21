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
package com.kodality.blaze.store;

import com.kodality.blaze.core.api.conformance.ResourceDefinitionListener;
import com.kodality.blaze.core.service.conformance.ConformanceHolder;
import com.kodality.blaze.store.dao.ResourceFunctionsDao;
import org.apache.commons.collections4.CollectionUtils;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;

@Component(immediate = true, service = ResourceDefinitionListener.class)
public class ResourceInitializer implements ResourceDefinitionListener {
  @Reference
  private ResourceFunctionsDao resourceFunctionsDao;

  @Activate
  private void init() {
    comply(ConformanceHolder.getDefinitions());
  }

  @Override
  public void comply(List<StructureDefinition> definitions) {
    if (CollectionUtils.isEmpty(definitions)) {
      return;
    }
    String domainResource = "http://hl7.org/fhir/StructureDefinition/DomainResource";
    definitions.stream()
        .filter(def -> domainResource.equals(def.getBaseDefinition()) || def.getName().equals("Binary"))
        .forEach(d -> resourceFunctionsDao.defineResource(d.getName()));
  }

}
