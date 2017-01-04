package com.nortal.blaze.search;

import com.nortal.blaze.search.dao.ResourceStructureDao;
import com.nortal.blaze.search.model.StructureElement;
import com.nortal.fhir.conformance.content.ResourceDefinitionListener;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.hl7.fhir.dstu3.model.ElementDefinition;
import org.hl7.fhir.dstu3.model.ElementDefinition.TypeRefComponent;
import org.hl7.fhir.dstu3.model.StructureDefinition;

@Component(immediate = true)
@Service(ResourceDefinitionListener.class)
public class StructureDefinitionUpdater implements ResourceDefinitionListener {
  @Reference
  private ResourceStructureDao structureDefinitionDao;

  @Override
  public void comply(List<StructureDefinition> definitions) {
    // TODO: check if already up to date
    structureDefinitionDao.deleteAll();
    if (CollectionUtils.isEmpty(definitions)) {
      return;
    }
    definitions.forEach(d -> saveDefinition(d));
  }

  private void saveDefinition(StructureDefinition def) {
    List<String> many = new ArrayList<String>();
    List<StructureElement> elements = new ArrayList<>(def.getSnapshot().getElement().size());
    for (ElementDefinition elementDef : def.getSnapshot().getElement()) {
      elements.add(new StructureElement(elementDef.getPath(), toCodeArray(elementDef.getType())));
      if (isMany(elementDef)) {
        many.add(elementDef.getPath());
      }
    }
    for (StructureElement element : elements) {
      element.setMany(CollectionUtils.containsAny(many, parents(element.getPath())));
    }

    structureDefinitionDao.create(elements);
  }

  private static List<String> parents(String path) {
    List<String> result = new ArrayList<String>();
    result.add(path);
    while (path.contains(".")) {
      path = StringUtils.substringBeforeLast(path, ".");
      result.add(path);
    }
    return result;
  }

  private boolean isMany(ElementDefinition elementDef) {
    if (elementDef.getMax() == null) {
      return false;
    }
    if (!elementDef.getPath().contains(".")) {// is root
      return false;
    }
    return elementDef.getMax().equals("*") || Integer.valueOf(elementDef.getMax()) > 1;
  }

  private static String[] toCodeArray(List<TypeRefComponent> types) {
    return types.stream().map(t -> t.getCode()).distinct().toArray(String[]::new);
  }

}
