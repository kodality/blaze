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
package com.nortal.blaze.core.service;

import com.nortal.blaze.core.api.conformance.ResourceDefinitionListener;
import com.nortal.blaze.core.service.conformance.ConformanceHolder;
import com.nortal.blaze.fhir.structure.service.ResourceFormatService;

import ca.uhn.fhir.context.FhirContext;

import org.hl7.fhir.r4.context.BaseWorkerContext;
import org.hl7.fhir.r4.context.IWorkerContext;
import org.hl7.fhir.r4.hapi.ctx.DefaultProfileValidationSupport;
import org.hl7.fhir.r4.hapi.ctx.HapiWorkerContext;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.utils.FHIRPathEngine;
import org.hl7.fhir.exceptions.FHIRException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.IOException;
import java.util.List;

@Component(immediate = true, service = { FhirPath.class, ResourceDefinitionListener.class })
public class FhirPath implements ResourceDefinitionListener {
  private FHIRPathEngine engine;
  @Reference
  private ResourceFormatService formatService;

  @Activate
  private void init() {
    IWorkerContext fhirContext = new HapiWorkerContext(FhirContext.forR4(), new DefaultProfileValidationSupport());
    engine = new FHIRPathEngine(fhirContext);
    //    comply(ConformanceHolder.getDefinitions());
  }

  @Override
  public void comply(List<StructureDefinition> definition) {
    //    if (definition == null) {
    //      return;
    //    }
    //    try {
    //            IWorkerContext fhirContext = SimpleWorkerContext.fromDefinitions(definition);
    //      ((BaseWorkerContext) fhirContext).setCanRunWithoutTerminology(true);
    //      engine = new FHIRPathEngine(fhirContext);
    //    } catch (IOException | FHIRException e) {
    //      throw new RuntimeException("fhir fhir ");
    //    }
  }

  @SuppressWarnings("unchecked")
  public <T> List<T> evaluate(Resource resource, String expression) {
    try {
      return (List<T>) engine.evaluate(resource, expression);
    } catch (FHIRException e) {
      throw new RuntimeException(e);
    }
  }

  public <T> List<T> evaluate(String content, String expression) {
    return evaluate(formatService.parse(content), expression);
  }

}
