package com.nortal.fhir.conformance.content;

import com.nortal.blaze.core.exception.ServerException;
import com.nortal.blaze.core.iface.ResourceValidator;
import com.nortal.blaze.core.model.ResourceContent;
import org.hl7.fhir.dstu3.model.StructureDefinition;

import java.util.List;

//@Component(immediate = true)
//@Service(value = { ResourceValidator.class, ResourceDefinitionListener.class })
public class ResourceProfileValidator implements ResourceValidator, ResourceDefinitionListener {
//  private IWorkerContext fhirContext;

  @Override
  public void validate(String type, ResourceContent content) {
    StructureDefinition definition = ResourceDefinitionsMonitor.getDefinition(type);
    if (definition == null) {
      throw new ServerException("definition for " + type + " not found");
    }
//    InstanceValidator validator = new InstanceValidator(fhirContext);
//    List<ValidationMessage> messages = new ArrayList<ValidationMessage>();
//    ByteArrayInputStream input = new ByteArrayInputStream(content.getBytes());
//    try {
//      validator.validate(null, messages, input, FhirFormat.XML);
//    } catch (Exception e) {
//      throw new RuntimeException(":/", e);
//    }
//    if (!messages.isEmpty()) {
//      throw new FhirParseException(StringUtils.join(messages, ","));
//    }

  }

  @Override
  public void comply(List<StructureDefinition> definition) {
//    try {
//      fhirContext = SimpleWorkerContext.fromDefinitions(definition);
//    } catch (IOException | FHIRException e) {
//      throw new RuntimeException("fhir fhir ");
//    }
  }

}
