package com.nortal.fhir.conformance.operations;

import com.nortal.blaze.core.exception.ServerException;
import com.nortal.blaze.core.util.EtcMonitor;
import com.nortal.blaze.core.util.Osgi;
import com.nortal.blaze.fhir.structure.api.ResourceComposer;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.dstu3.model.SearchParameter;

@Component(immediate = true)
public class SearchParameterMonitor extends EtcMonitor {
  protected static final String GLOBAL = "Resource";
  protected static final List<SearchParameter> all = new ArrayList<SearchParameter>();
  protected static final Map<String, Map<String, SearchParameter>> parameters = new HashMap<>();

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
    Resource res = ResourceComposer.parse(file);
    if (ResourceType.SearchParameter != res.getResourceType()) {
      return;
    }
    SearchParameter sp = (SearchParameter) res;
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
    Osgi.getBeans(SearchParameterListener.class).forEach(l -> l.comply(all));
  }

}
