package com.nortal.blaze.store;

import com.nortal.blaze.store.dao.ResourceFunctionsDao;
import com.nortal.fhir.conformance.content.ResourceDefinitionListener;
import com.nortal.fhir.conformance.content.ResourceDefinitionsMonitor;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.hl7.fhir.dstu3.model.StructureDefinition;

@Component(immediate = true)
@Service(ResourceDefinitionListener.class)
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
