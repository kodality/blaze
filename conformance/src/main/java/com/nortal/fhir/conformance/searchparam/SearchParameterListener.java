package com.nortal.fhir.conformance.searchparam;

import java.util.List;
import org.hl7.fhir.dstu3.model.SearchParameter;

public interface SearchParameterListener {
  void comply(List<SearchParameter> params);
}