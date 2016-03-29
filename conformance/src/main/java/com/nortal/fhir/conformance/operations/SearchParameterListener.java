package com.nortal.fhir.conformance.operations;

import java.util.List;
import org.hl7.fhir.instance.model.SearchParameter;

public interface SearchParameterListener {
  void comply(List<SearchParameter> params);
}
