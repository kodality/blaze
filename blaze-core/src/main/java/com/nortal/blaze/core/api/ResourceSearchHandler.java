package com.nortal.blaze.core.api;

import com.nortal.blaze.core.model.search.SearchCriterion;
import com.nortal.blaze.core.model.search.SearchResult;

public interface ResourceSearchHandler {
  SearchResult search(SearchCriterion criteria);
}
