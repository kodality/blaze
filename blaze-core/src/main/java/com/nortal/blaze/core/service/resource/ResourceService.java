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

import com.nortal.blaze.core.api.resource.ResourceAfterDeleteInterceptor;
import com.nortal.blaze.core.api.resource.ResourceAfterSaveInterceptor;
import com.nortal.blaze.core.api.resource.ResourceBeforeSaveInterceptor;
import com.nortal.blaze.core.api.resource.ResourceSearchHandler;
import com.nortal.blaze.core.api.resource.ResourceStorehouse;
import com.nortal.blaze.core.model.ResourceId;
import com.nortal.blaze.core.model.ResourceVersion;
import com.nortal.blaze.core.model.VersionId;
import com.nortal.blaze.core.model.search.HistorySearchCriterion;
import com.nortal.blaze.core.util.ResourceUtil;
import com.nortal.blaze.fhir.structure.api.ResourceContent;
import com.nortal.blaze.tx.TransactionService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nortal.blaze.core.api.resource.ResourceAfterSaveInterceptor.FINALIZATION;
import static com.nortal.blaze.core.api.resource.ResourceBeforeSaveInterceptor.BUSINESS_VALIDATION;
import static com.nortal.blaze.core.api.resource.ResourceBeforeSaveInterceptor.INPUT_VALIDATION;
import static com.nortal.blaze.core.api.resource.ResourceBeforeSaveInterceptor.NORMALIZATION;
import static org.osgi.service.component.annotations.ReferenceCardinality.MULTIPLE;
import static org.osgi.service.component.annotations.ReferenceCardinality.OPTIONAL;
import static org.osgi.service.component.annotations.ReferencePolicy.DYNAMIC;

@Component(immediate = true, service = ResourceService.class)
public class ResourceService {
  @Reference(cardinality = OPTIONAL)
  private volatile ResourceStorehouse storehouse;
  @Reference(cardinality = OPTIONAL)
  private volatile ResourceSearchHandler searchHandler;
  @Reference
  private TransactionService tx;

  @Reference(cardinality = MULTIPLE, policy = DYNAMIC, service = ResourceBeforeSaveInterceptor.class)
  private final Map<String, List<ResourceBeforeSaveInterceptor>> beforeSaveInterceptors = new HashMap<>();
  @Reference(cardinality = MULTIPLE, policy = DYNAMIC, service = ResourceAfterSaveInterceptor.class)
  private final Map<String, List<ResourceAfterSaveInterceptor>> afterSaveInterceptors = new HashMap<>();
  @Reference(cardinality = MULTIPLE, policy = DYNAMIC, service = ResourceAfterDeleteInterceptor.class)
  private final List<ResourceAfterDeleteInterceptor> afterDeleteInterceptor = new ArrayList<>();

  public ResourceVersion save(ResourceId id, ResourceContent content, String interaction) {
    interceptBeforeSave(INPUT_VALIDATION, id, content, interaction);
    interceptBeforeSave(NORMALIZATION, id, content, interaction);
    interceptBeforeSave(BUSINESS_VALIDATION, id, content, interaction);

    id.setResourceId(id.getResourceId() == null ? generateNewId() : id.getResourceId());
    ResourceVersion version = tx.transaction(() -> {
      interceptBeforeSave(ResourceBeforeSaveInterceptor.TRANSACTION, id, content, interaction);
      ResourceVersion ver = store(id, content);
      interceptAfterSave(ResourceAfterSaveInterceptor.TRANSACTION, ver);
      return ver;
    });
    interceptAfterSave(FINALIZATION, version);
    return version;
  }

  /**
   * @param reference  ResourceType/id
   */
  public ResourceVersion load(String reference) {
    return load(ResourceUtil.parseReference(reference));
  }

  public ResourceVersion load(VersionId id) {
    return storehouse.load(id);
  }

  public List<ResourceVersion> loadHistory(HistorySearchCriterion criteria) {
    return storehouse.loadHistory(criteria);
  }

  public void delete(ResourceId id) {
    storehouse.delete(id);
    afterDeleteInterceptor.forEach(i -> i.delete(id));
  }

  /**
   * use with caution. only business logic
   * inside transaction
   */
  public ResourceVersion store(ResourceId id, ResourceContent content) {
    return storehouse.save(id, content);
  }
  
  /**
   * use with caution. only business logic
   * outside of transaction
   */
  public ResourceVersion storeForce(ResourceId id, ResourceContent content) {
    return storehouse.saveForce(id, content);
  }

  public String generateNewId() {
    return storehouse.generateNewId();
  }

  private void interceptBeforeSave(String phase, ResourceId id, ResourceContent content, String interaction) {
    if (beforeSaveInterceptors.containsKey(phase)) {
      beforeSaveInterceptors.get(phase).forEach(i -> i.handle(id, content, interaction));
    }
  }

  private void interceptAfterSave(String phase, ResourceVersion version) {
    if (afterSaveInterceptors.containsKey(phase)) {
      afterSaveInterceptors.get(phase).forEach(i -> i.handle(version));
    }
  }

  protected void bind(ResourceBeforeSaveInterceptor interceptor) {
    this.beforeSaveInterceptors.computeIfAbsent(interceptor.getPhase(), p -> new ArrayList<>()).add(interceptor);
  }

  protected void unbind(ResourceBeforeSaveInterceptor interceptor) {
    this.beforeSaveInterceptors.values().forEach(l -> l.remove(interceptor));
  }

  protected void bind(ResourceAfterSaveInterceptor interceptor) {
    this.afterSaveInterceptors.computeIfAbsent(interceptor.getPhase(), p -> new ArrayList<>()).add(interceptor);
  }

  protected void unbind(ResourceAfterSaveInterceptor interceptor) {
    this.afterSaveInterceptors.values().forEach(l -> l.remove(interceptor));
  }

  protected void bind(ResourceAfterDeleteInterceptor interceptor) {
    this.afterDeleteInterceptor.add(interceptor);
  }

  protected void unbind(ResourceAfterDeleteInterceptor interceptor) {
    this.afterDeleteInterceptor.remove(interceptor);
  }

}
