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
package com.nortal.blaze.store;

import com.nortal.blaze.auth.ClientIdentity;
import com.nortal.blaze.core.api.resource.ResourceStorehouse;
import com.nortal.blaze.core.exception.FhirException;
import com.nortal.blaze.core.model.ResourceId;
import com.nortal.blaze.core.model.ResourceVersion;
import com.nortal.blaze.core.model.VersionId;
import com.nortal.blaze.core.model.search.HistorySearchCriterion;
import com.nortal.blaze.core.service.cache.CacheManager;
import com.nortal.blaze.core.util.DateUtil;
import com.nortal.blaze.core.util.JsonUtil;
import com.nortal.blaze.fhir.structure.api.ResourceComposer;
import com.nortal.blaze.fhir.structure.api.ResourceContent;
import com.nortal.blaze.store.dao.ResourceDao;
import com.nortal.blaze.util.sql.PgTransactionManager;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;
import org.hl7.fhir.r4.model.Resource;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(immediate = true, service = ResourceStorehouse.class)
public class PostgreStorehouse implements ResourceStorehouse {
  @Reference
  private ClientIdentity clientIdentity;
  @Reference
  private ResourceDao resourceDao;
  @Reference
  private PgTransactionManager tx;
  @Reference
  private CacheManager cache;

  @Activate
  private void init() {
    cache.registerCache("pgCache", 2000, 64);
  }

  @Override
  public ResourceVersion save(ResourceId id, ResourceContent content) {
    return tx.transaction(() -> store(id, content));
  }

  @Override
  public ResourceVersion saveForce(ResourceId id, ResourceContent content) {
    DefaultTransactionAttribute prop = new DefaultTransactionAttribute(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    return tx.transaction(prop, () -> store(id, content));
  }

  private ResourceVersion store(ResourceId id, ResourceContent content) {
    ResourceContent cont = content.getContentType().contains("json") ? content : toJson(content);
    ResourceVersion version = new ResourceVersion(new VersionId(id), cont);
    version.getId().setVersion(resourceDao.getLastVersion(id) + 1);
    if (clientIdentity.get() != null) {
      version.setAuthor(clientIdentity.get().getClaims());
    }
    resourceDao.create(version);
    cache.removeKeys("pgCache", version.getId().getResourceReference());
    return version;
  }

  private ResourceContent toJson(ResourceContent content) {
    Resource resource = ResourceComposer.parse(content.getValue());
    return ResourceComposer.compose(resource, "json");
  }

  @Override
  public String generateNewId() {
    return resourceDao.getNextResourceId();
  }

  @Override
  public void delete(ResourceId id) {
    tx.transaction(() -> {
      ResourceVersion current = resourceDao.load(new VersionId(id));
      if (current.isDeleted()) {
        return;
      }
      ResourceVersion version = new ResourceVersion();
      version.setId(new VersionId(id));
      version.setDeleted(true);
      version.getId().setVersion(resourceDao.getLastVersion(id) + 1);
      if (clientIdentity.get() != null) {
        version.setAuthor(clientIdentity.get().getClaims());
      }
      resourceDao.create(version);
      cache.removeKeys("pgCache", version.getId().getResourceReference());
    });
  }

  @Override
  public ResourceVersion load(VersionId id) {
    //FIXME: when transaction is rolled back this cache breaks everything
    //    ResourceVersion version = cache.get("pgCache", id.getReference(), () -> resourceDao.load(id));
    ResourceVersion version = resourceDao.load(id);
    if (version == null) {
      throw new FhirException(404, IssueType.NOTFOUND, id.getReference() + " not found");
    }
    decorate(version);

    return version;
  }

  @Override
  public List<ResourceVersion> loadHistory(HistorySearchCriterion criteria) {
    List<ResourceVersion> history = resourceDao.loadHistory(criteria);
    history.forEach(this::decorate);
    return history;
  }

  @SuppressWarnings("unchecked")
  private void decorate(ResourceVersion version) {
    // TODO: maybe rewrite this when better times come and resource will be parsed until end.

    Map<String, Object> resource =
        version.getContent().getValue() != null ? JsonUtil.fromJson(version.getContent().getValue()) : new HashMap<>();
    resource.put("id", version.getId().getResourceId());
    resource.put("resourceType", version.getId().getResourceType());
    Map<Object, Object> meta = (Map<Object, Object>) resource.getOrDefault("meta", new HashMap<>());
    meta.put("versionId", "" + version.getId().getVersion());
    meta.put("lastUpdated", new SimpleDateFormat(DateUtil.FHIR_DATETIME).format(version.getModified()));
    resource.put("meta", meta);

    version.getContent().setValue(JsonUtil.toJson(resource));
  }

}
