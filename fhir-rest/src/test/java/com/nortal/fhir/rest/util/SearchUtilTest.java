package com.nortal.fhir.rest.util;

import com.nortal.blaze.core.model.search.QueryParam;
import com.nortal.fhir.rest.SearchConformance;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import org.hl7.fhir.instance.model.Conformance;
import org.hl7.fhir.instance.model.Conformance.ConformanceRestComponent;
import org.hl7.fhir.instance.model.Conformance.ConformanceRestResourceComponent;
import org.hl7.fhir.instance.model.Conformance.ConformanceRestResourceSearchParamComponent;
import org.hl7.fhir.instance.model.Conformance.RestfulConformanceMode;
import org.hl7.fhir.instance.model.Conformance.SearchModifierCode;
import org.hl7.fhir.instance.model.Enumerations.SearchParamType;
import org.hl7.fhir.instance.model.ResourceType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SearchUtilTest {
  @Before
  public void mocks() {
    Conformance conformance = new Conformance();
    ConformanceRestComponent rest = conformance.addRest();
    rest.setMode(RestfulConformanceMode.SERVER);
    ConformanceRestResourceComponent resource = rest.addResource();
    resource.setType(ResourceType.Patient.name());
    addSearchParam(resource, s -> {
      s.setName("papa");
      s.setType(SearchParamType.REFERENCE);
      s.addTarget(ResourceType.Patient.name());
    });
    addSearchParam(resource, s -> {
      s.setName("name");
      s.setType(SearchParamType.STRING);
      s.addModifier(SearchModifierCode.EXACT);
    });
    new SearchConformance().comply(conformance);
  }

  private void addSearchParam(ConformanceRestResourceComponent resource,
                              Consumer<ConformanceRestResourceSearchParamComponent> a) {
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
