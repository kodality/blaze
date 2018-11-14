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
 package com.nortal.blaze.core.service.resource;

import com.nortal.blaze.core.api.resource.ResourceSearchHandler;
import com.nortal.blaze.core.exception.FhirServerException;
import com.nortal.blaze.core.model.ResourceVersion;
import com.nortal.blaze.core.model.VersionId;
import com.nortal.blaze.core.model.search.SearchCriterion;
import com.nortal.blaze.core.model.search.SearchResult;
import com.nortal.blaze.core.service.FhirPath;
import com.nortal.blaze.core.service.conformance.ConformanceHolder;
import com.nortal.blaze.core.util.ResourceUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.Enumerations.SearchParamType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Component(immediate = true, service = ResourceSearchService.class)
public class ResourceSearchService {
  private static final String INCLUDE_ALL = "*";
  @Reference(cardinality = ReferenceCardinality.OPTIONAL)
  private volatile ResourceSearchHandler searchHandler;
  @Reference
  private ResourceService resourceService;
  @Reference
  private FhirPath fhirPath;

  public SearchResult search(String resourceType, String[]... params) {
    Map<String, List<String>> map = Stream.of(params).collect(toMap(p -> p[0], p -> Collections.singletonList(p[1])));
    return search(new SearchCriterion(resourceType, SearchUtil.parse(map, resourceType)));
  }

  public SearchResult search(String resourceType, Map<String, List<String>> params) {
    return search(new SearchCriterion(resourceType, SearchUtil.parse(params, resourceType)));
  }

  public SearchResult search(SearchCriterion criteria) {
    if (searchHandler == null) {
      throw new FhirServerException(500, "search module not installed");
    }
    SearchResult result = searchHandler.search(criteria);
    result.getEntries()
        .stream()
        .filter(e -> e.getContent() == null)
        .forEach(e -> e.setContent(resourceService.load(e.getId()).getContent()));
    include(result, criteria);
    revInclude(result, criteria);
    return result;
  }

  private void include(SearchResult result, SearchCriterion criteria) {
    //TODO: :recursive
    criteria.getResultParams(SearchCriterion._INCLUDE).forEach(ip -> ip.getValues().forEach(includeKey -> {
      String[] includeTokens = SearchUtil.parseInclude(includeKey);
      String resourceType = includeTokens[0];
      String searchParam = includeTokens[1];
      String targetType = includeTokens[2];
      List<String> expressions = findReferenceParams(resourceType, searchParam);
      result.getEntries().stream().filter(e -> e.getId().getResourceType().equals(resourceType)).forEach(entry -> {
        expressions.stream()
            .flatMap(expr -> evaluate(entry, expr))
            .map(ref -> ResourceUtil.parseReference(ref.getReference()))
            .filter(reference -> targetType == null || reference.getResourceType().equals(targetType))
            .filter(reference -> !contains(result, reference))
            .forEach(reference -> result.addInclude(resourceService.load(reference)));
      });
    }));
  }

  private void revInclude(SearchResult result, SearchCriterion criteria) {
    //TODO: :recursive
    if (CollectionUtils.isEmpty(criteria.getResultParams(SearchCriterion._REVINCLUDE))) {
      return;
    }
    List<String> entryRefs = result.getEntries().stream().map(e -> e.getId().getResourceReference()).collect(toList());
    if (CollectionUtils.isEmpty(entryRefs)) {
      return;
    }
    criteria.getResultParams(SearchCriterion._REVINCLUDE).forEach(ip -> ip.getValues().forEach(includeKey -> {
      String[] includeTokens = SearchUtil.parseInclude(includeKey);
      String resourceType = includeTokens[0];
      String searchParam = includeTokens[1];
      String targetType = includeTokens[2];
      if (searchParam.equals(INCLUDE_ALL)) {
        throw new FhirServerException(501, "revinclude '*' not currently supported");
      }
      if (targetType != null && !targetType.equals(criteria.getType())) {
        return;
      }
      Map<String, List<String>> revSearch = new HashMap<>(1);
      revSearch.put(searchParam, Collections.singletonList(StringUtils.join(entryRefs, ",")));
      revSearch.put(SearchCriterion._COUNT, Collections.singletonList("1000"));// would kill search anyway if more
      result.addIncludes(search(resourceType, revSearch).getEntries());
    }));
  }

  private boolean contains(SearchResult result, VersionId reference) {
    return Stream.concat(result.getEntries().stream(), result.getIncludes().stream()).anyMatch(e -> {
      //TODO: if reference is non-versioned - last version must be included.
      //      need to make sure if included resource is already last version or not.
      return reference.getVersion() == null
                                            ? e.getId().getResourceReference().equals(reference.getResourceReference())
                                            : e.getId().getReference().equals(reference.getReference());
    });
  }

  private Stream<org.hl7.fhir.dstu3.model.Reference> evaluate(ResourceVersion entry, String expr) {
    return fhirPath.<org.hl7.fhir.dstu3.model.Reference> evaluate(entry.getContent().getValue(), expr).stream();
  }

  private List<String> findReferenceParams(String resourceType, String param) {
    return ConformanceHolder.getSearchParams(resourceType)
        .stream()
        .filter(sp -> param.equals(INCLUDE_ALL) || sp.getCode().equals(param))
        .filter(sp -> sp.getType() == SearchParamType.REFERENCE)
        .map(sp -> sp.getExpression())
        .collect(toList());
  }

}
