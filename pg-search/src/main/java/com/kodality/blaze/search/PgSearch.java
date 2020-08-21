/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 package com.kodality.blaze.search;

import com.kodality.blaze.core.api.resource.ResourceSearchHandler;
import com.kodality.blaze.core.model.ResourceVersion;
import com.kodality.blaze.core.model.search.SearchCriterion;
import com.kodality.blaze.core.model.search.SearchResult;
import com.kodality.blaze.core.util.JsonUtil;
import com.kodality.blaze.search.dao.PgSearchDao;
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
