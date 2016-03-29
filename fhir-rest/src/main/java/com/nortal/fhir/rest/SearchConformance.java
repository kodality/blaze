package com.nortal.fhir.rest;

import com.nortal.fhir.conformance.operations.ConformanceListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.hl7.fhir.instance.model.Conformance;
import org.hl7.fhir.instance.model.Conformance.ConformanceRestComponent;
import org.hl7.fhir.instance.model.Conformance.ConformanceRestResourceComponent;
import org.hl7.fhir.instance.model.Conformance.ConformanceRestResourceSearchParamComponent;
import org.hl7.fhir.instance.model.Conformance.RestfulConformanceMode;

@Component(immediate = true)
@Service(value = ConformanceListener.class)
public class SearchConformance implements ConformanceListener {
  private static final Map<String, Map<String, ConformanceRestResourceSearchParamComponent>> params = new HashMap<>();

  public static ConformanceRestResourceSearchParamComponent get(String resourceType, String element) {
    return params.getOrDefault(resourceType, Collections.emptyMap()).get(element);
  }

  @Override
  public void comply(Conformance conformance) {
    params.clear();
    if (conformance == null) {
      return;
    }
    for (ConformanceRestComponent rest : conformance.getRest()) {
      if (rest.getMode() == RestfulConformanceMode.SERVER) {
        params.put(null, map(rest.getSearchParam(), p -> p.getName()));
        rest.getResource().forEach(resource -> read(resource));
      }
    }
  }

  private void read(ConformanceRestResourceComponent resource) {
    Map<String, ConformanceRestResourceSearchParamComponent> all = new HashMap<>(params.get(null));
    all.putAll(map(resource.getSearchParam(), p -> p.getName()));
    params.put(resource.getType(), all);
  }

  // XXX guess there should be some util like this?
  private static <K, V> Map<K, V> map(List<V> list, Function<V, K> key) {
    return list.stream().collect(Collectors.toMap(key, v -> v));
  }

}
