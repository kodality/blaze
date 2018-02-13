package com.nortal.blaze.store;

import com.nortal.blaze.core.api.conformance.ResourceDefinitionListener;
import com.nortal.blaze.core.service.conformance.ConformanceHolder;
import com.nortal.blaze.store.dao.ResourceFunctionsDao;
import org.apache.commons.collections4.CollectionUtils;
import org.hl7.fhir.dstu3.model.StructureDefinition;
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
        .filter(def -> domainResource.equals(def.getBaseDefinition()))
        .forEach(d -> resourceFunctionsDao.defineResource(d.getName()));
  }

}
