package com.nortal;

import com.nortal.blaze.core.model.search.QueryParam;
import com.nortal.blaze.core.service.conformance.CapabilitySearchConformance;
import com.nortal.blaze.core.service.resource.SearchUtil;
import org.hl7.fhir.dstu3.model.CapabilityStatement;
import org.hl7.fhir.dstu3.model.CapabilityStatement.CapabilityStatementRestComponent;
import org.hl7.fhir.dstu3.model.CapabilityStatement.CapabilityStatementRestResourceComponent;
import org.hl7.fhir.dstu3.model.CapabilityStatement.CapabilityStatementRestResourceSearchParamComponent;
import org.hl7.fhir.dstu3.model.CapabilityStatement.RestfulCapabilityMode;
import org.hl7.fhir.dstu3.model.Enumerations.SearchParamType;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class SearchUtilTest {
  @Before
  public void mocks() {
    CapabilityStatement capability = new CapabilityStatement();
    CapabilityStatementRestComponent rest = capability.addRest();
    rest.setMode(RestfulCapabilityMode.SERVER);
    CapabilityStatementRestResourceComponent resource = rest.addResource();
    resource.setType(ResourceType.Patient.name());
    addSearchParam(resource, s -> {
      s.setName("papa");
      s.setType(SearchParamType.REFERENCE);
//      s.addTarget(ResourceType.Patient.name());
    });
    addSearchParam(resource, s -> {
      s.setName("name");
      s.setType(SearchParamType.STRING);
//      s.addModifier(SearchModifierCode.EXACT);
    });
    new CapabilitySearchConformance().comply(capability);
  }

  private void addSearchParam(CapabilityStatementRestResourceComponent resource,
                              Consumer<CapabilityStatementRestResourceSearchParamComponent> a) {
    a.accept(resource.addSearchParam());
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
    Assert.assertEquals(q.get(0).getChain().getChain().getChain().getChain().getValues().get(0), "фарадей");
  }
}
