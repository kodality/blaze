package com.nortal.blaze.search;

import com.nortal.blaze.core.api.resource.ResourceSearchHandler;
import com.nortal.blaze.core.model.ResourceVersion;
import com.nortal.blaze.core.model.search.SearchCriterion;
import com.nortal.blaze.core.model.search.SearchResult;
import com.nortal.blaze.core.util.JsonUtil;
import com.nortal.blaze.search.dao.PgSearchDao;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.Map;

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
    result.stream().forEach(vers -> {
      // TODO: maybe rewrite this when better times come and resource will be parsed until end.
      Map<String, Object> hack = JsonUtil.fromJson(vers.getContent().getValue());
      hack.put("id", vers.getId().getResourceId());
      vers.getContent().setValue(JsonUtil.toJson(hack));
    });
    return new SearchResult(total, result);
  }

}
