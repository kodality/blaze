package com.nortal.blaze.core.service.conformance;

import com.nortal.blaze.core.api.conformance.CapabilityStatementListener;
import org.hl7.fhir.dstu3.model.CapabilityStatement;
import org.hl7.fhir.dstu3.model.CapabilityStatement.CapabilityStatementRestComponent;
import org.hl7.fhir.dstu3.model.CapabilityStatement.CapabilityStatementRestResourceComponent;
import org.hl7.fhir.dstu3.model.CapabilityStatement.CapabilityStatementRestResourceSearchParamComponent;
import org.hl7.fhir.dstu3.model.CapabilityStatement.RestfulCapabilityMode;
import org.osgi.service.component.annotations.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component(immediate = true, service = CapabilityStatementListener.class)
public class CapabilitySearchConformance implements CapabilityStatementListener {
  // resource type -> search param key -> search param
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
    Map<String, CapabilityStatementRestResourceSearchParamComponent> globalParams = params.get(null);
    Map<String, CapabilityStatementRestResourceSearchParamComponent> all = new HashMap<>(globalParams);
    all.putAll(map(resource.getSearchParam(), p -> p.getName()));
    params.put(resource.getType(), all);
  }

  private static <K, V> Map<K, V> map(List<V> list, Function<V, K> key) {
    return list.stream().collect(Collectors.toMap(key, v -> v));
  }

}