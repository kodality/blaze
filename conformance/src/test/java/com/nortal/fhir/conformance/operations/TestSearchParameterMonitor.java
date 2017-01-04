package com.nortal.fhir.conformance.operations;

import java.util.HashMap;
import org.hl7.fhir.dstu3.model.SearchParameter;

public class TestSearchParameterMonitor extends SearchParameterMonitor {

  public static void apply(SearchParameter sp) {
    all.add(sp);
    sp.getBase().forEach(ct -> {
      parameters.putIfAbsent(ct.getValue(), new HashMap<String, SearchParameter>());
      parameters.get(ct.getValue()).put(sp.getCode(), sp);
    });
  }
}
