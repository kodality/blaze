package com.nortal.blaze.core.model.search;

import com.nortal.blaze.core.model.ResourceVersion;
import com.nortal.blaze.core.model.VersionId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;

public class SearchResult {
  private Integer total;
  private List<ResourceVersion> entries;

  public SearchResult() {
    this(0, new ArrayList<ResourceVersion>());
  }

  public SearchResult(Integer total, List<ResourceVersion> entries) {
    this.total = total;
    this.entries = entries;
  }

  public static SearchResult lazy(Integer total, List<VersionId> ids) {
    List<ResourceVersion> versions = new ArrayList<>();
    for (VersionId id : ids) {
      versions.add(new ResourceVersion(id, null));
    }
    return new SearchResult(total, versions);
  }

  public static SearchResult empty() {
    return new SearchResult(0, Collections.<ResourceVersion> emptyList());
  }

  public boolean isEmpty() {
    return CollectionUtils.isEmpty(entries);
  }

  public Integer getTotal() {
    return total;
  }

  public void setTotal(Integer total) {
    this.total = total;
  }

  public List<ResourceVersion> getEntries() {
    return entries;
  }

  public void setEntries(List<ResourceVersion> entries) {
    this.entries = entries;
  }

}
