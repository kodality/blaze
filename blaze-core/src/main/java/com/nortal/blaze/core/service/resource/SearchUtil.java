package com.nortal.blaze.core.service.resource;

import com.nortal.blaze.core.exception.FhirBadRequestException;
import com.nortal.blaze.core.exception.ServerException;
import com.nortal.blaze.core.model.search.QueryParam;
import com.nortal.blaze.core.model.search.SearchCriterion;
import com.nortal.blaze.core.service.conformance.CapabilitySearchConformance;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.CapabilityStatement.CapabilityStatementRestResourceSearchParamComponent;
import org.hl7.fhir.dstu3.model.Enumerations.SearchParamType;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public final class SearchUtil {
  private static final String UTF8 = "UTF8";
  private static final String MODIFIER = ":";
  private static final String CHAIN = ".";

  private SearchUtil() {
    //
  }

  public static List<QueryParam> parse(String query, String resourceType) {
    Map<String, List<String>> params = new HashMap<>();
    Stream.of(query.split("&")).forEach(q -> {
      String[] qr = q.split("=");
      params.computeIfAbsent(qr[0], (a) -> new ArrayList<>()).add(qr[1]);
    });
    return parse(params, resourceType);
  }

  public static List<QueryParam> parse(Map<String, List<String>> params, String resourceType) {
    if (params == null || params.isEmpty()) {
      return Collections.emptyList();
    }
    List<QueryParam> result = new ArrayList<>();
    params.forEach((k, v) -> result.addAll(parse(k, v, resourceType)));
    return result;
  }

  public static List<QueryParam> parse(String rawKey, List<String> rawValues, String resourceType) {
    ChainForge chainsmith = buildForge(rawKey, resourceType);
    return rawValues.stream().map(value -> {
      QueryParam param = chainsmith.forge();
      param.setValues(Arrays.asList(StringUtils.split(decode(value), ",")));
      return param;
    }).collect(toList());
  }

  private static String decode(String s) {
    try {
      return URLDecoder.decode(s, UTF8);
    } catch (UnsupportedEncodingException e) {
      throw new ServerException("there are two ways to write error-free programs");
    }
  }

  private static ChainForge buildForge(String chain, String resourceType) {
    String link = StringUtils.substringBefore(chain, CHAIN);
    String key = StringUtils.substringBefore(link, MODIFIER);
    String modifier = link.contains(MODIFIER) ? StringUtils.substringAfter(link, MODIFIER) : null;

    if (SearchCriterion.resultParamKeys.contains(key)) {
      return new ChainForge(key, modifier, null, resourceType);
    }

    CapabilityStatementRestResourceSearchParamComponent conformance =
        CapabilitySearchConformance.get(resourceType, key);
    validate(conformance, key, modifier);

    ChainForge forge = new ChainForge(key, modifier, conformance.getType(), resourceType);
    if (chain.contains(CHAIN)) {
      String remainder = chain.contains(CHAIN) ? StringUtils.substringAfter(chain, CHAIN) : null;
      if (modifier == null) {
        throw new FhirBadRequestException("sorry, but specify reference resource type: " + key);
      }
      forge.nextLink(buildForge(remainder, modifier));
    }
    return forge;
  }

  private static void validate(CapabilityStatementRestResourceSearchParamComponent conformance,
                               String key,
                               String modifier) {
    if (conformance == null) {
      throw new FhirBadRequestException("search parameter '" + key + "' not supported by conformance");
    }
    if (!validateModifier(conformance, modifier)) {
      throw new FhirBadRequestException("modifier '" + modifier + "' not supported by conformance for '" + key + "'");
    }
  }

  private static boolean validateModifier(CapabilityStatementRestResourceSearchParamComponent conformance,
                                          String modifier) {
    if (StringUtils.isEmpty(modifier)) {
      return true;
    }
    // FIXME
    return true;
    // if (conformance.getType() == SearchParamType.REFERENCE) {
    // return CollectionUtils.isEmpty(conformance.getTarget())
    // || conformance.getTarget().stream().anyMatch(t -> t.getValue().equals(modifier));
    // }
    // return conformance.getModifier().stream().anyMatch(m -> m.getValue().toCode().equals(modifier));
  }

  private static class ChainForge {
    private final String key;
    private final String modifier;
    private final SearchParamType paramType;
    private final String resourceType;

    private ChainForge next;

    public ChainForge(String key, String modifier, SearchParamType paramType, String resourceType) {
      this.key = key;
      this.modifier = modifier;
      this.paramType = paramType;
      this.resourceType = resourceType;
    }

    public void nextLink(ChainForge next) {
      this.next = next;
    }

    public QueryParam forge() {
      QueryParam param = new QueryParam(key, modifier, paramType, resourceType);
      if (next != null) {
        param.setChain(next.forge());
      }
      return param;
    }

  }

}