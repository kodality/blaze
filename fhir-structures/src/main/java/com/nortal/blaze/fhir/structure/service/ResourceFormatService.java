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
package com.nortal.blaze.fhir.structure.service;

import com.nortal.blaze.fhir.structure.api.ParseException;
import com.nortal.blaze.fhir.structure.api.ResourceContent;
import com.nortal.blaze.fhir.structure.api.ResourceRepresentation;
import com.nortal.blaze.fhir.structure.defs.JsonRepresentation;
import com.nortal.blaze.fhir.structure.defs.XmlRepresentation;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
// import org.apache.felix.scr.annotations.Reference;
// import org.apache.felix.scr.annotations.ReferenceCardinality;
// import org.apache.felix.scr.annotations.ReferencePolicy;
import org.hl7.fhir.dstu3.model.Resource;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Duration;
import java.util.Optional;
import java.util.stream.Stream;

@Component(immediate = true, service = ResourceFormatService.class)
public class ResourceFormatService {
  @Reference
  private JsonRepresentation jsonRepresentation;
  @Reference
  private XmlRepresentation xmlRepresentation;
  private Cache<String, ? extends Resource> cache;

  @Activate
  private void init() {
    CacheManager manager = CacheManagerBuilder.newCacheManagerBuilder().build();
    manager.init();
    CacheConfigurationBuilder<String, Resource> builder = CacheConfigurationBuilder
        .newCacheConfigurationBuilder(String.class, Resource.class, ResourcePoolsBuilder.heap(2048));
    builder.withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(32)));
    cache = manager.createCache("resources", builder.build());
  }

  public ResourceContent compose(Resource resource, String mime) {
    if (resource == null) {
      return null;
    }
    ResourceRepresentation presenter =
        findPresenter(mime).orElse(findPresenter("json").orElseThrow(() -> new ParseException("unknown format")));
    return new ResourceContent(presenter.compose(resource), presenter.getFhirFormat().getExtension());
  }

  public <R extends Resource> R parse(ResourceContent content) {
    return parse(content.getValue());
  }
  
  @SuppressWarnings("unchecked")
  public <R extends Resource> R parse(String input) {
    String key = DigestUtils.md5Hex(input);
    if (cache.get(key) == null) {
      cache.put(key,
                guessPresenter(input)
                    .orElseThrow(() -> new ParseException("unknown format: [" + StringUtils.left(input, 10) + "]"))
                    .parse(input));
    }
    return (R) cache.get(key).copy();
  }

  public Optional<ResourceRepresentation> findPresenter(String ct) {
    if (ct == null) {
      return Optional.empty();
    }
    String mime = StringUtils.substringBefore(ct, ";");
    return Stream.of(jsonRepresentation, xmlRepresentation).filter(c -> c.getMimeTypes().contains(mime)).findFirst();
    // return presenters.stream().filter(c -> c.getMimeTypes().contains(mime)).findFirst();
  }

  private Optional<ResourceRepresentation> guessPresenter(String content) {
    return Stream.of(jsonRepresentation, xmlRepresentation).filter(c -> c.isParsable(content)).findFirst();
    // return presenters.stream().filter(c -> c.isParsable(content)).findFirst();
  }

}
