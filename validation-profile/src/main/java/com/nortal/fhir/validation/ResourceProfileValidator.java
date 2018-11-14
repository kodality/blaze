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
package com.nortal.fhir.validation;

import com.nortal.blaze.core.api.conformance.ResourceDefinitionListener;
import com.nortal.blaze.core.api.resource.OperationInterceptor;
import com.nortal.blaze.core.api.resource.ResourceBeforeSaveInterceptor;
import com.nortal.blaze.core.exception.FhirException;
import com.nortal.blaze.core.exception.FhirServerException;
import com.nortal.blaze.core.model.ResourceId;
import com.nortal.blaze.core.service.conformance.ConformanceHolder;
import com.nortal.blaze.fhir.structure.api.ParseException;
import com.nortal.blaze.fhir.structure.api.ResourceContent;
import com.nortal.blaze.fhir.structure.api.ResourceRepresentation;
import com.nortal.blaze.fhir.structure.service.ResourceFormatService;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.context.BaseWorkerContext;
import org.hl7.fhir.dstu3.context.IWorkerContext;
import org.hl7.fhir.dstu3.context.SimpleWorkerContext;
import org.hl7.fhir.dstu3.elementmodel.Element;
import org.hl7.fhir.dstu3.elementmodel.Manager.FhirFormat;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.dstu3.model.OperationOutcome.IssueType;
import org.hl7.fhir.dstu3.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.dstu3.model.StructureDefinition;
import org.hl7.fhir.dstu3.validation.InstanceValidator;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.utilities.validation.ValidationMessage;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Component(immediate = true, service = { ResourceBeforeSaveInterceptor.class, ResourceDefinitionListener.class,
                                         OperationInterceptor.class })
public class ResourceProfileValidator extends ResourceBeforeSaveInterceptor
    implements ResourceDefinitionListener, OperationInterceptor {
  @Reference
  private ResourceFormatService representationService;
  private IWorkerContext fhirContext;

  public ResourceProfileValidator() {
    super(ResourceBeforeSaveInterceptor.INPUT_VALIDATION);
  }

  @Activate
  private void init() {
    comply(ConformanceHolder.getDefinitions());
  }

  @Override
  public void comply(List<StructureDefinition> definition) {
    if (definition == null) {
      return;
    }
    try {
      fhirContext = SimpleWorkerContext.fromDefinitions(definition);
      ((BaseWorkerContext) fhirContext).setCanRunWithoutTerminology(true);
    } catch (IOException | FHIRException e) {
      throw new RuntimeException("fhir fhir ");
    }
  }

  @Override
  public void handle(String level, String operation, ResourceContent parameters) {
    if (StringUtils.isEmpty(parameters.getValue())) {
      return;
    }
    runValidation(ResourceType.Parameters.name(), parameters);
  }

  @Override
  public void handle(ResourceId id, ResourceContent content, String interaction) {
    String resourceType = id.getResourceType();
    runValidation(resourceType, content);

  }

  private void runValidation(String resourceType, ResourceContent content) {
    StructureDefinition definition = ConformanceHolder.getDefinition(resourceType);
    if (definition == null) {
      throw new FhirServerException(500, "definition for " + resourceType + " not found");
    }
    if (fhirContext == null) {
      throw new FhirServerException(500, "fhir context initialization error");
    }
    List<ValidationMessage> errors = validate(resourceType, content);
    errors = errors.stream().filter(m -> isError(m.getLevel())).collect(toList());
    if (!errors.isEmpty()) {
      throw new FhirException(400, errors.stream().map(msg -> {
        OperationOutcomeIssueComponent issue = new OperationOutcomeIssueComponent();
        issue.setCode(IssueType.INVALID);
        issue.setSeverity(severity(msg));
        issue.setDetails(new CodeableConcept().setText(msg.getMessage()));
        issue.addLocation(msg.getLocation());
        return issue;
      }).collect(toList()));
    }
  }

  private boolean isError(org.hl7.fhir.utilities.validation.ValidationMessage.IssueSeverity level) {
    return level == org.hl7.fhir.utilities.validation.ValidationMessage.IssueSeverity.ERROR
        || level == org.hl7.fhir.utilities.validation.ValidationMessage.IssueSeverity.FATAL;
  }

  private IssueSeverity severity(ValidationMessage msg) {
    try {
      return IssueSeverity.fromCode(msg.getLevel().toCode());
    } catch (FHIRException e) {
      throw new RuntimeException("спасибо вам.", e);
    }
  }

  private List<ValidationMessage> validate(String resourceType, ResourceContent content) {
    Element element;
    List<ValidationMessage> messages = new ArrayList<ValidationMessage>();
    try {
      InstanceValidator validator = new InstanceValidator(fhirContext, null);
      ByteArrayInputStream input = new ByteArrayInputStream(content.getBytes());
      element = validator.validate(null, messages, input, getFhirFormat(content));
    } catch (Exception e) {
      throw new RuntimeException(":/", e);
    }
    if (element != null && !element.getType().equals(resourceType)) {
      String msg = "was expecting " + resourceType + " but found " + element.getType();
      throw new FhirException(400, IssueType.INVALID, msg);
    }
    return messages;
  }

  private FhirFormat getFhirFormat(ResourceContent content) {
    String ct = StringUtils.substringBefore(content.getContentType(), ";");
    ResourceRepresentation repr =
        representationService.findPresenter(ct).orElseThrow(() -> new ParseException("unknown format"));
    return repr.getFhirFormat();
  }

}
