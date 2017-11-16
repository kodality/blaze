package com.nortal.fhir.conformance.content;

import com.nortal.blaze.core.util.EtcMonitor;
import com.nortal.blaze.core.util.Osgi;
import com.nortal.blaze.fhir.structure.api.ResourceComposer;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.dstu3.model.StructureDefinition;
import org.hl7.fhir.dstu3.validation.ProfileValidator;
import org.hl7.fhir.utilities.validation.ValidationMessage;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@Component(immediate = true)
public class ResourceDefinitionsMonitor extends EtcMonitor {
  private static final Map<String, StructureDefinition> definitions = new HashMap<>();

  public ResourceDefinitionsMonitor() {
    super("definitions");
  }

  @Activate
  private void init() {
    start();
  }

  @Deactivate
  private void destroy() {
    stop();
  }

  public static List<StructureDefinition> get() {
    return new ArrayList<StructureDefinition>(definitions.values());
  }

  public static StructureDefinition getDefinition(String type) {
    return definitions.get(type);
  }

  @Override
  protected void clear() {
    definitions.clear();
  }

  @Override
  protected void file(File file) {
    Resource res = ResourceComposer.parse(file);
    if (ResourceType.StructureDefinition != res.getResourceType()) {
      return;
    }
    StructureDefinition definition = (StructureDefinition) res;
    validate(definition);
    definitions.put(definition.getName(), definition);
  }

  @Override
  protected void finish() {
    Osgi.getBeans(ResourceDefinitionListener.class).forEach(l -> l.comply(get()));
  }

  private void validate(StructureDefinition definition) {
    List<ValidationMessage> errors = new ProfileValidator().validate(definition, false);
    if (CollectionUtils.isEmpty(errors)) {
      return;
    }
    throw new RuntimeException(errors.stream().map(e -> e.getMessage()).collect(Collectors.joining(",")));
  }

}
