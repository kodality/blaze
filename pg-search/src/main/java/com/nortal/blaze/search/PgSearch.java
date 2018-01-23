package com.nortal.blaze.search;

import com.nortal.blaze.core.iface.ResourceSearchHandler;
import com.nortal.blaze.core.model.ResourceVersion;
import com.nortal.blaze.core.model.search.SearchCriterion;
import com.nortal.blaze.core.model.search.SearchResult;
import com.nortal.blaze.search.dao.PgSearchDao;
import java.util.List;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(immediate = true, service = ResourceSearchHandler.class)
public class PgSearch implements ResourceSearchHandler {
  @Reference
  private PgSearchDao pgSearchDao;

  @Override
  public SearchResult search(SearchCriterion criteria) {
    Integer total = pgSearchDao.count(criteria);
    if (total == 0) {
      return SearchResult.empty();
    }
    List<ResourceVersion> result = pgSearchDao.search(criteria);
    return new SearchResult(total, result);
  }

}
