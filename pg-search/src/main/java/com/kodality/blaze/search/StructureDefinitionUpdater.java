/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 package com.kodality.blaze.search;

import com.kodality.blaze.core.api.conformance.ResourceDefinitionListener;
import com.kodality.blaze.core.service.conformance.ConformanceHolder;
import com.kodality.blaze.search.model.StructureElement;
import com.kodality.blaze.search.dao.ResourceStructureDao;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.ElementDefinition;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.ArrayList;
import java.util.List;

@Component(immediate = true)
public class StructureDefinitionUpdater implements ResourceDefinitionListener {
  @Reference
  private ResourceStructureDao structureDefinitionDao;

  @Activate
  private void init() {
    comply(ConformanceHolder.getDefinitions());
  }

  @Override
  public void comply(List<StructureDefinition> definitions) {
    // TODO: check if already up to date
    structureDefinitionDao.deleteAll();
    if (CollectionUtils.isEmpty(definitions)) {
      return;
    }
    definitions.forEach(d -> saveDefinition(d));
    structureDefinitionDao.refresh();
  }

  private void saveDefinition(StructureDefinition def) {
    List<String> many = new ArrayList<String>();
    List<StructureElement> elements = new ArrayList<>(def.getSnapshot().getElement().size());
    for (ElementDefinition elementDef : def.getSnapshot().getElement()) {
      if (elementDef.getId().contains(":")) {
        // XXX think if we should and how should we store definitions with modifier (:). problem - they have same path
        // Quantity vs Quantity:simplequantity
        return;
      }

      elementDef.getType().stream().map(t -> t.getCode()).distinct().forEach(type -> {
        String path = StringUtils.replace(elementDef.getPath(), "[x]", StringUtils.capitalize(type));
        elements.add(new StructureElement(StringUtils.substringBefore(path, "."),
                                          StringUtils.substringAfter(path, "."),
                                          type));
      });
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

}
