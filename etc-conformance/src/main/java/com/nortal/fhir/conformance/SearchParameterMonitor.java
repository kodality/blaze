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
 package com.nortal.fhir.conformance;

import com.nortal.blaze.core.service.conformance.ConformanceHolder;
import com.nortal.blaze.core.util.EtcMonitor;
import com.nortal.blaze.fhir.structure.service.ResourceFormatService;
import org.apache.commons.io.FileUtils;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.dstu3.model.SearchParameter;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component(immediate = true)
public class SearchParameterMonitor extends EtcMonitor {
  protected static final String GLOBAL = "Resource";
  protected static final Map<String, Map<String, SearchParameter>> parameters = new HashMap<>();
  @Reference
  private ResourceFormatService representationService;
  @Reference
  private ConformanceHolder conformanceHolder;

  public SearchParameterMonitor() {
    super("searchparameter");
  }

  @Activate
  private void init() {
    start();
  }

  @Deactivate
  private void destroy() {
    stop();
  }

  @Override
  protected void clear() {
    parameters.clear();
  }

  @Override
  protected void file(File file) {
    Resource res = representationService.parse(readFile(file));
    if (ResourceType.SearchParameter == res.getResourceType()) {
      readSearchParam((SearchParameter) res);
    }
    if (ResourceType.Bundle == res.getResourceType()) {
      Bundle bundle = (Bundle) res;
      bundle.getEntry().stream().forEach(e -> {
        if (ResourceType.SearchParameter == e.getResource().getResourceType()) {
          readSearchParam((SearchParameter) e.getResource());
        }
      });
    }
  }

  private void readSearchParam(SearchParameter sp) {
    sp.getBase().forEach(ct -> parameters.computeIfAbsent(ct.getValue(), (k) -> new HashMap<>()).put(sp.getCode(), sp));
  }

  @Override
  protected void finish() {
    Map<String, SearchParameter> globalParams = parameters.get(GLOBAL);
    for (Map<String, SearchParameter> resourceParameters : parameters.values()) {
      globalParams.forEach((k, v) -> resourceParameters.putIfAbsent(k, v));
    }
    conformanceHolder.setSearchParams(parameters);
  }

  private String readFile(File file) {
    try {
      return FileUtils.readFileToString(file, "UTF8");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
