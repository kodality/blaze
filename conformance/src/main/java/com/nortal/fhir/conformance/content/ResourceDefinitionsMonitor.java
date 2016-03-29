package com.nortal.fhir.conformance.content;

import com.nortal.blaze.core.util.EtcMonitor;
import com.nortal.blaze.core.util.Osgi;
import com.nortal.blaze.representation.ResourceParser;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;
import org.hl7.fhir.instance.model.StructureDefinition;
import org.hl7.fhir.instance.validation.ProfileValidator;

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
    Resource res = ResourceParser.parse(file);
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
    try {
      List<String> errors = new ProfileValidator().validate(definition);
      if (CollectionUtils.isEmpty(errors)) {
        return;
      }
      throw new RuntimeException(StringUtils.join(errors, "; "));
    } catch (Exception e) {
      throw new RuntimeException("боги ексепшнов", e);
    }
  }

}
