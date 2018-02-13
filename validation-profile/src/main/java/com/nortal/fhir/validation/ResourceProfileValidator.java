package com.nortal.fhir.validation;

import com.nortal.blaze.core.api.conformance.ResourceDefinitionListener;
import com.nortal.blaze.core.api.resource.ResourceValidator;
import com.nortal.blaze.core.exception.FhirException;
import com.nortal.blaze.core.exception.ServerException;
import com.nortal.blaze.core.model.ResourceContent;
import com.nortal.blaze.core.service.conformance.ConformanceHolder;
import com.nortal.blaze.fhir.structure.api.ParseException;
import com.nortal.blaze.fhir.structure.api.ResourceRepresentation;
import com.nortal.blaze.fhir.structure.service.ResourceRepresentationService;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.context.BaseWorkerContext;
import org.hl7.fhir.dstu3.context.IWorkerContext;
import org.hl7.fhir.dstu3.context.SimpleWorkerContext;
import org.hl7.fhir.dstu3.elementmodel.Manager.FhirFormat;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.dstu3.model.OperationOutcome.OperationOutcomeIssueComponent;
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

@Component(immediate = true, service = { ResourceValidator.class, ResourceDefinitionListener.class })
public class ResourceProfileValidator implements ResourceValidator, ResourceDefinitionListener {
  @Reference
  private ResourceRepresentationService representationService;
  private IWorkerContext fhirContext;

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
  public void validate(String type, ResourceContent content) {
    //    if(true) return;
    StructureDefinition definition = ConformanceHolder.getDefinition(type);
    if (definition == null) {
      throw new ServerException("definition for " + type + " not found");
    }
    if (fhirContext == null) {
      throw new ServerException("fhir context initialization error");
    }
    List<ValidationMessage> errors = validate(content);
    errors = errors.stream().filter(m -> isError(m.getLevel())).collect(toList());
    if (!errors.isEmpty()) {
      throw new FhirException(400, errors.stream().map(msg -> {
        OperationOutcomeIssueComponent issue = new OperationOutcomeIssueComponent();
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

  private List<ValidationMessage> validate(ResourceContent content) {
    try {
      List<ValidationMessage> messages = new ArrayList<ValidationMessage>();
      InstanceValidator validator = new InstanceValidator(fhirContext, null);
      ByteArrayInputStream input = new ByteArrayInputStream(content.getBytes());
      validator.validate(null, messages, input, getFhirFormat(content));
      return messages;
    } catch (Exception e) {
      throw new RuntimeException(":/", e);
    }
  }

  private FhirFormat getFhirFormat(ResourceContent content) {
    String ct = StringUtils.substringBefore(content.getContentType(), ";");
    ResourceRepresentation repr =
        representationService.findPresenter(ct).orElseThrow(() -> new ParseException("unknown format"));
    return repr.getFhirFormat();
  }

}
