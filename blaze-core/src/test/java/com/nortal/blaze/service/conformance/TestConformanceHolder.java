package com.nortal.blaze.service.conformance;

import com.nortal.blaze.core.service.conformance.ConformanceHolder;
import org.hl7.fhir.dstu3.model.SearchParameter;

import java.util.HashMap;

public class TestConformanceHolder extends ConformanceHolder {

  public static void apply(SearchParameter sp) {
    sp.getBase().forEach(ct -> {
      searchParams.putIfAbsent(ct.getValue(), new HashMap<String, SearchParameter>());
      searchParams.get(ct.getValue()).put(sp.getCode(), sp);
    });
  }
}
