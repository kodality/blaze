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
 package com.kodality.fhir.validation;

import com.kodality.blaze.core.api.resource.OperationInterceptor;
import com.kodality.blaze.core.api.resource.ResourceBeforeSaveInterceptor;
import com.kodality.blaze.core.exception.FhirException;
import com.kodality.blaze.core.model.ResourceId;
import com.kodality.blaze.fhir.structure.api.ResourceContent;
import com.kodality.blaze.fhir.structure.service.ResourceFormatService;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(immediate = true, service = { ResourceBeforeSaveInterceptor.class, OperationInterceptor.class })
public class StupidResourceValidator extends ResourceBeforeSaveInterceptor implements OperationInterceptor {
  public StupidResourceValidator() {
    super(ResourceBeforeSaveInterceptor.INPUT_VALIDATION);
  }

  @Reference
  private ResourceFormatService resourceRepresentationService;

  @Override
  public void handle(String level, String operation, ResourceContent parameters) {
    if (StringUtils.isEmpty(parameters.getValue())) {
      return;
    }
    validate(parameters);
  }

  @Override
  public void handle(ResourceId id, ResourceContent content, String interaction) {
    validate(content);
  }

  private void validate(ResourceContent content) {
    try {
      resourceRepresentationService.parse(content.getValue());
    } catch (Exception e) {
      throw new FhirException(400, IssueType.STRUCTURE, "error during resource parse: " + e.getMessage());
    }
  }

}
