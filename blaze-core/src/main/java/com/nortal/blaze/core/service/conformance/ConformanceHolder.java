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
 package com.nortal.blaze.core.service.conformance;

import com.nortal.blaze.core.api.conformance.CapabilityStatementListener;
import com.nortal.blaze.core.api.conformance.ResourceDefinitionListener;
import com.nortal.blaze.core.api.conformance.SearchParameterListener;
import com.nortal.blaze.core.exception.FhirException;
import org.hl7.fhir.dstu3.model.CapabilityStatement;
import org.hl7.fhir.dstu3.model.OperationOutcome.IssueType;
import org.hl7.fhir.dstu3.model.SearchParameter;
import org.hl7.fhir.dstu3.model.StructureDefinition;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Component(immediate = true, service = ConformanceHolder.class)
public class ConformanceHolder {
  protected static CapabilityStatement capabilityStatement;
  protected static Map<String, StructureDefinition> definitions = new HashMap<>();
  //resource type -> code -> param
  protected static Map<String, Map<String, SearchParameter>> searchParams = new HashMap<>();

  @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
  private final List<CapabilityStatementListener> capabilityListeners = new ArrayList<>();
  @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
  private final List<SearchParameterListener> searchParamListeners = new ArrayList<>();
  @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
  private final List<ResourceDefinitionListener> definitionListeners = new ArrayList<>();

  public static CapabilityStatement getCapabilityStatement() {
    return capabilityStatement;
  }

  public static List<StructureDefinition> getDefinitions() {
    return new ArrayList<StructureDefinition>(definitions.values());
  }

  public static StructureDefinition getDefinition(String type) {
    return definitions.get(type);
  }

  public static List<SearchParameter> getSearchParams() {
    return searchParams.values().stream().flatMap(m -> m.values().stream()).collect(toList());
  }

  public static List<SearchParameter> getSearchParams(String type) {
    if (!searchParams.containsKey(type)) {
      return null;
    }
    return new ArrayList<>(searchParams.get(type).values());
  }

  public static SearchParameter getSearchParam(String type, String code) {
    if (!searchParams.containsKey(type)) {
      return null;
    }
    return searchParams.get(type).get(code);
  }

  public static SearchParameter requireSearchParam(String type, String code) {
    SearchParameter param = getSearchParam(type, code);
    if (param == null) {
      String details = type + "/" + code + " searchparam does not exist in search config";
      throw new FhirException(400, IssueType.NOTSUPPORTED, details);
    }
    return param;
  }

  public void setCapabilityStatement(CapabilityStatement capabilityStatement) {
    ConformanceHolder.capabilityStatement = capabilityStatement;
    capabilityListeners.forEach(l -> l.comply(capabilityStatement));
  }

  public void setDefinitions(Map<String, StructureDefinition> definitions) {
    ConformanceHolder.definitions = definitions;
    definitionListeners.forEach(l -> l.comply(getDefinitions()));
  }

  public void setSearchParams(Map<String, Map<String, SearchParameter>> searchParams) {
    ConformanceHolder.searchParams = searchParams;
    searchParamListeners.forEach(l -> l.comply(getSearchParams()));
  }

  protected void bind(CapabilityStatementListener listener) {
    capabilityListeners.add(listener);
  }

  protected void unbind(CapabilityStatementListener listener) {
    capabilityListeners.remove(listener);
  }

  protected void bind(SearchParameterListener listener) {
    searchParamListeners.add(listener);
  }

  protected void unbind(SearchParameterListener listener) {
    searchParamListeners.remove(listener);
  }

  protected void bind(ResourceDefinitionListener listener) {
    definitionListeners.add(listener);
  }

  protected void unbind(ResourceDefinitionListener listener) {
    definitionListeners.remove(listener);
  }

}
