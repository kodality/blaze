package com.nortal.fhir.conformance.content;

import com.nortal.blaze.core.exception.ServerException;
import com.nortal.blaze.core.iface.ResourceValidator;
import com.nortal.blaze.core.model.ResourceContent;
import org.hl7.fhir.dstu3.model.StructureDefinition;

//@Provides
//@Component
//@Instantiate
public class ResourceProfileValidator implements ResourceValidator {

  @Override
  public void validate(String type, ResourceContent content) {
    StructureDefinition definition = ResourceDefinitionsMonitor.getDefinition(type);
    if (definition == null) {
      throw new ServerException("definition for " + type + " not found");
    }
    try {
//       ValidationEngine validator = new ValidationEngine();
//       validator.init();
//       validator.setProfile(definition);
//       validator.setSource(content.getBytes());
//       validator.setNoSchematron(true);
//       validator.process();
    } catch (Exception e) {
      throw new RuntimeException(":/", e);
    }

  }

}
