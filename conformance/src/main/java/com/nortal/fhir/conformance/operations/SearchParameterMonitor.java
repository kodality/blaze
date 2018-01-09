package com.nortal.fhir.conformance.operations;

import com.nortal.blaze.core.exception.ServerException;
import com.nortal.blaze.core.util.EtcMonitor;
import com.nortal.blaze.fhir.structure.service.ResourceRepresentationService;
import org.apache.commons.io.FileUtils;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.dstu3.model.SearchParameter;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(immediate = true)
public class SearchParameterMonitor extends EtcMonitor {
  protected static final String GLOBAL = "Resource";
  protected static final List<SearchParameter> all = new ArrayList<SearchParameter>();
  protected static final Map<String, Map<String, SearchParameter>> parameters = new HashMap<>();
  @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
  private final List<SearchParameterListener> listeners = new ArrayList<>();
  @Reference
  private ResourceRepresentationService representationService;

  public SearchParameterMonitor() {
    super("searchparameter");
  }

  @Activate
  private void init() {
    start();
  }

  @Deactivate
  private void destroy() {
    stop();
  }

  @Override
  protected void clear() {
    parameters.clear();
  }

  public static List<SearchParameter> get() {
    return all;
  }

  public static List<SearchParameter> get(String type) {
    if (!parameters.containsKey(type)) {
      return null;
    }
    return new ArrayList<>(parameters.get(type).values());
  }

  public static SearchParameter get(String type, String code) {
    if (!parameters.containsKey(type)) {
      return null;
    }
    return parameters.get(type).get(code);
  }

  public static SearchParameter require(String type, String code) {
    SearchParameter param = get(type, code);
    if (param == null) {
      throw new ServerException(type + "/" + code + " searchparam does not exist in search config");
    }
    return param;
  }

  @Override
  protected void file(File file) {
    Resource res = representationService.parse(readFile(file));
    if (ResourceType.SearchParameter == res.getResourceType()) {
      readSearchParam((SearchParameter) res);
    }
    if (ResourceType.Bundle == res.getResourceType()) {
      Bundle bundle = (Bundle) res;
      bundle.getEntry().stream().forEach(e -> {
        if (ResourceType.SearchParameter == e.getResource().getResourceType()) {
          readSearchParam((SearchParameter) e.getResource());
        }
      });
    }
  }

  private void readSearchParam(SearchParameter sp) {
    sp.getBase().forEach(ct -> {
      String key = ct.getValue();
      parameters.putIfAbsent(key, new HashMap<String, SearchParameter>());
      parameters.get(key).put(sp.getCode(), sp);
      all.add(sp);
    });
  }

  @Override
  protected void finish() {
    Map<String, SearchParameter> globalParams = parameters.get(GLOBAL);
    for (Map<String, SearchParameter> resourceParameters : parameters.values()) {
      globalParams.forEach((k, v) -> resourceParameters.putIfAbsent(k, v));
    }
    listeners.forEach(l -> l.comply(all));
  }

  protected void bind(SearchParameterListener listener) {
    listeners.add(listener);
  }

  protected void unbind(SearchParameterListener listener) {
    listeners.remove(listener);
  }

  private String readFile(File file) {
    try {
      return FileUtils.readFileToString(file, "UTF8");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
