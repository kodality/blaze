package com.nortal.fhir.rest;

import com.nortal.fhir.conformance.operations.CapabilityStatementListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.hl7.fhir.dstu3.model.CapabilityStatement;
import org.hl7.fhir.dstu3.model.CapabilityStatement.CapabilityStatementRestComponent;
import org.hl7.fhir.dstu3.model.CapabilityStatement.CapabilityStatementRestResourceComponent;
import org.hl7.fhir.dstu3.model.CapabilityStatement.CapabilityStatementRestResourceSearchParamComponent;
import org.hl7.fhir.dstu3.model.CapabilityStatement.RestfulCapabilityMode;

@Component(immediate = true)
@Service(value = CapabilityStatementListener.class)
public class SearchConformance implements CapabilityStatementListener {
  private static final Map<String, Map<String, CapabilityStatementRestResourceSearchParamComponent>> params =
      new HashMap<>();

  public static CapabilityStatementRestResourceSearchParamComponent get(String resourceType, String element) {
    return params.getOrDefault(resourceType, Collections.emptyMap()).get(element);
  }

  @Override
  public void comply(CapabilityStatement capabilityStatement) {
    params.clear();
    if (capabilityStatement == null) {
      return;
    }
    for (CapabilityStatementRestComponent rest : capabilityStatement.getRest()) {
      if (rest.getMode() == RestfulCapabilityMode.SERVER) {
        params.put(null, map(rest.getSearchParam(), p -> p.getName()));
        rest.getResource().forEach(resource -> read(resource));
      }
    }
  }

  private void read(CapabilityStatementRestResourceComponent resource) {
    Map<String, CapabilityStatementRestResourceSearchParamComponent> all = new HashMap<>(params.get(null));
    all.putAll(map(resource.getSearchParam(), p -> p.getName()));
    params.put(resource.getType(), all);
  }

  private static <K, V> Map<K, V> map(List<V> list, Function<V, K> key) {
    return list.stream().collect(Collectors.toMap(key, v -> v));
  }

}
