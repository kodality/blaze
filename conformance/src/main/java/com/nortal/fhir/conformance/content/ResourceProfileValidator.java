package com.nortal.fhir.conformance.content;

import com.nortal.blaze.core.exception.FhirParseException;
import com.nortal.blaze.core.exception.ServerException;
import com.nortal.blaze.core.iface.ResourceValidator;
import com.nortal.blaze.core.model.ResourceContent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.context.IWorkerContext;
import org.hl7.fhir.dstu3.context.SimpleWorkerContext;
import org.hl7.fhir.dstu3.elementmodel.Manager.FhirFormat;
import org.hl7.fhir.dstu3.model.StructureDefinition;
import org.hl7.fhir.dstu3.validation.InstanceValidator;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.utilities.validation.ValidationMessage;

//@Component(immediate = true)
//@Service(value = { ResourceValidator.class, ResourceDefinitionListener.class })
public class ResourceProfileValidator implements ResourceValidator, ResourceDefinitionListener {
  private IWorkerContext fhirContext;

  @Override
  public void validate(String type, ResourceContent content) {
    StructureDefinition definition = ResourceDefinitionsMonitor.getDefinition(type);
    if (definition == null) {
      throw new ServerException("definition for " + type + " not found");
    }
    InstanceValidator validator = new InstanceValidator(fhirContext);
    List<ValidationMessage> messages = new ArrayList<ValidationMessage>();
    ByteArrayInputStream input = new ByteArrayInputStream(content.getBytes());
    try {
      validator.validate(null, messages, input, FhirFormat.XML);
    } catch (Exception e) {
      throw new RuntimeException(":/", e);
    }
    if (!messages.isEmpty()) {
      throw new FhirParseException(StringUtils.join(messages, ","));
    }

  }

  @Override
  public void comply(List<StructureDefinition> definition) {
    try {
      fhirContext = SimpleWorkerContext.fromDefinitions(definition);
    } catch (IOException | FHIRException e) {
      throw new RuntimeException("fhir fhir ");
    }
  }

}
