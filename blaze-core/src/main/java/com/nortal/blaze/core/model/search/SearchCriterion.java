package com.nortal.blaze.core.model.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;

public class SearchCriterion {
  public static final String _SORT = "_sort";
  public static final String _COUNT = "_count";
  public static final String _PAGE = "_page";
  public static final String _REVINCLUDE = "_revinclude";
  public static final String _SUMMARY = "_summary";
  public static final String _ELEMENTS = "_elements";
  public static final String _CONTAINED = "_contained";
  public static final String _CONTAINEDTYPE = "_containedType";
  public static final List<String> resultParamKeys =
      Arrays.asList(_SORT, _COUNT, _PAGE, _REVINCLUDE, _SUMMARY, _ELEMENTS, _CONTAINED, _CONTAINEDTYPE);

  private String type;
  private List<QueryParam> chains;
  private List<QueryParam> conditions;
  private List<QueryParam> resultParams;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public List<QueryParam> getChains() {
    return chains;
  }

  public List<QueryParam> getConditions() {
    return conditions;
  }

  public List<QueryParam> getResultParams(String key) {
    return resultParams.stream().filter(q -> q.getKey().equals(key)).collect(Collectors.toList());
  }

  public void setParams(List<QueryParam> params) {
    chains = new ArrayList<>();
    conditions = new ArrayList<>();
    resultParams = new ArrayList<>();
    if (CollectionUtils.isEmpty(params)) {
      return;
    }
    for (QueryParam param : params) {
      if (param.getChain() != null) {
        chains.add(param);
      } else if (resultParamKeys.contains(param.getKey())) {
        resultParams.add(param);
      } else {
        conditions.add(param);
      }
    }
  }

  public Integer getCount() {
    List<QueryParam> _count = getResultParams(_COUNT);
    if (CollectionUtils.isEmpty(_count) || CollectionUtils.isEmpty(_count.get(0).getValues())) {
      return 10;
    }
    return ObjectUtils.max(0, Integer.valueOf(_count.get(0).getValues().get(0)));
  }

  public Integer getPage() {
    List<QueryParam> _page = getResultParams(_PAGE);
    if (CollectionUtils.isEmpty(_page) || CollectionUtils.isEmpty(_page.get(0).getValues())) {
      return 1;
    }
    return ObjectUtils.max(1, Integer.valueOf(_page.get(0).getValues().get(0)));
  }

}