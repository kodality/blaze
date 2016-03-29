package com.nortal.blaze.core.iface;

import com.nortal.blaze.core.model.search.SearchCriterion;
import com.nortal.blaze.core.model.search.SearchResult;

public interface Google {
  SearchResult search(SearchCriterion criteria);
}
