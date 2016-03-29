package com.nortal.fhir.conformance.operations;

import java.util.HashMap;
import org.hl7.fhir.instance.model.SearchParameter;

public class TestSearchParameterMonitor extends SearchParameterMonitor {

  public static void apply(SearchParameter sp) {
    all.add(sp);
    String key = sp.getBase();
    parameters.putIfAbsent(key, new HashMap<String, SearchParameter>());
    parameters.get(key).put(sp.getCode(), sp);
  }
}
