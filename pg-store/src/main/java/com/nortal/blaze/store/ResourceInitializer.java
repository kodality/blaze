package com.nortal.blaze.store;

import com.nortal.blaze.store.dao.ResourceFunctionsDao;
import com.nortal.fhir.conformance.content.ResourceDefinitionListener;
import com.nortal.fhir.conformance.content.ResourceDefinitionsMonitor;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.hl7.fhir.dstu3.model.StructureDefinition;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(immediate = true, service = ResourceDefinitionListener.class)
public class ResourceInitializer implements ResourceDefinitionListener {
  @Reference
  private ResourceFunctionsDao resourceFunctionsDao;

  @Activate
  private void init() {
    comply(ResourceDefinitionsMonitor.get());
  }

  @Override
  public void comply(List<StructureDefinition> definitions) {
    if (CollectionUtils.isEmpty(definitions)) {
      return;
    }
    definitions.forEach(d -> resourceFunctionsDao.defineResource(d.getName()));
  }

}
