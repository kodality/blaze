package com.nortal.blaze.core.iface;

import com.nortal.blaze.core.model.search.SearchCriterion;
import com.nortal.blaze.core.model.search.SearchResult;

public interface ResourceSearchHandler {
  SearchResult search(SearchCriterion criteria);
}
