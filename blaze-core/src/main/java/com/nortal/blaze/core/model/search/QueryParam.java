package com.nortal.blaze.core.model.search;

import java.util.List;
import org.apache.commons.lang3.Validate;
import org.hl7.fhir.dstu3.model.Enumerations.SearchParamType;

public class QueryParam {
  private final String key;
  private final String modifier;
  private final SearchParamType type;
  private final String resourceType;

  private QueryParam chain;
  private List<String> values;

  public QueryParam(String key, String modifier, SearchParamType type, String resourceType) {
    this.key = key;
    this.modifier = modifier;
    this.type = type;
    this.resourceType = resourceType;
  }

  public String getResourceType() {
    return resourceType;
  }

  public SearchParamType getType() {
    return type;
  }

  public String getKey() {
    return key;
  }

  public String getModifier() {
    return modifier;
  }

  public void setChain(QueryParam chain) {
    Validate.isTrue(values == null);
    this.chain = chain;
  }

  public QueryParam getChain() {
    return chain;
  }

  public void setValues(List<String> values) {
    if (chain != null) {
      chain.setValues(values);
      return;
    }
    this.values = values;
  }

  public List<String> getValues() {
    Validate.isTrue(chain == null);
    return values;
  }

}
