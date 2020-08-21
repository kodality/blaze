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
 package com.kodality;

import com.kodality.blaze.core.model.search.QueryParam;
import com.kodality.blaze.core.service.conformance.CapabilitySearchConformance;
import com.kodality.blaze.core.service.conformance.ConformanceHolder;
import com.kodality.blaze.core.service.resource.SearchUtil;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestResourceComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestResourceSearchParamComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.RestfulCapabilityMode;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.SearchParameter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchUtilTest {
  @Before
  public void mocks() {
    Map<String, Map<String, SearchParameter>> searchParams = new HashMap<>();
    CapabilityStatement capability = new CapabilityStatement();
    CapabilityStatementRestComponent rest = capability.addRest();
    rest.setMode(RestfulCapabilityMode.SERVER);

    CapabilityStatementRestResourceComponent resource = rest.addResource().setType(ResourceType.Patient.name());
    HashMap<String, SearchParameter> patientSearchParams = new HashMap<>();
    searchParams.put(ResourceType.Patient.name(), patientSearchParams);

    addSearchParam(resource.addSearchParam(), patientSearchParams, "papa", SearchParamType.REFERENCE);
    patientSearchParams.get("papa").addTarget(ResourceType.Patient.name());
    addSearchParam(resource.addSearchParam(), patientSearchParams, "name", SearchParamType.STRING);

    new CapabilitySearchConformance().comply(capability);
    new ConformanceHolder().setSearchParams(searchParams);
  }

  private void addSearchParam(CapabilityStatementRestResourceSearchParamComponent conformance,
                              Map<String, SearchParameter> searchParams,
                              String name,
                              SearchParamType type) {
    conformance.setName(name);
    conformance.setType(SearchParamType.REFERENCE);
    searchParams.put(name, new SearchParameter());
  }

  @Test
  public void chaintestNoChain() {
    List<QueryParam> q = SearchUtil.parse("name", Collections.singletonList("колян"), ResourceType.Patient.name());
    Assert.assertEquals(q.size(), 1);
    Assert.assertEquals(q.get(0).getValues().get(0), "колян");
  }

  @Test
  public void chaintest() {
    List<QueryParam> q = SearchUtil.parse("papa:Patient.papa:Patient.papa:Patient.papa:Patient.name:exact",
                                          Collections.singletonList("фарадей"),
                                          ResourceType.Patient.name());
    Assert.assertEquals(q.size(), 1);
    Assert.assertEquals(
                        q.get(0)
                            .getChains()
                            .get(0)
                            .getChains()
                            .get(0)
                            .getChains()
                            .get(0)
                            .getChains()
                            .get(0)
                            .getValues()
                            .get(0),
                        "фарадей");
  }
}
