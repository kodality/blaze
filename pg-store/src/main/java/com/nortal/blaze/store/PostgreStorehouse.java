package com.nortal.blaze.store;

import com.nortal.blaze.auth.ClientIdentity;
import com.nortal.blaze.core.api.ResourceStorehouse;
import com.nortal.blaze.core.exception.FhirNotFoundException;
import com.nortal.blaze.core.model.ResourceContent;
import com.nortal.blaze.core.model.ResourceId;
import com.nortal.blaze.core.model.ResourceVersion;
import com.nortal.blaze.core.model.VersionId;
import com.nortal.blaze.core.model.search.HistorySearchCriterion;
import com.nortal.blaze.core.util.DateUtil;
import com.nortal.blaze.core.util.JsonUtil;
import com.nortal.blaze.fhir.structure.api.ResourceComposer;
import com.nortal.blaze.store.dao.ResourceDao;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

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

  @Override
  public ResourceVersion save(VersionId id, ResourceContent content) {
    return tx.transaction(() -> {
      if (!content.getContentType().contains("json")) {
        content.setValue(ResourceComposer.compose(ResourceComposer.parse(content.getValue()), "json"));
      }
      ResourceVersion version = new ResourceVersion(id, content);
      version.getId().setVersion(resourceDao.getLastVersion(id) + 1);
      if (clientIdentity.get() != null) {
        version.setAuthor(clientIdentity.get().getClaims());
      }
      resourceDao.create(version);
      return version;
    });
  }

  @Override
  public String prepareId() {
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
    });
  }

  @Override
  public ResourceVersion load(VersionId id) {
    ResourceVersion version = resourceDao.load(id);
    if (version == null) {
      throw new FhirNotFoundException(id.getReference() + " not found");
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

  private void decorate(ResourceVersion version) {
    // TODO: maybe rewrite this when better times come and resource will be parsed until end.

    Map<String, Object> resource =
        version.getContent().getValue() != null ? JsonUtil.fromJson(version.getContent().getValue()) : new HashMap<>();
    resource.put("id", version.getId().getResourceId());
    resource.put("resourceType", version.getId().getResourceType());
    HashMap<Object, Object> meta = new HashMap<>();
    meta.put("versionId", "" + version.getId().getVersion());
    meta.put("lastUpdated", new SimpleDateFormat(DateUtil.FHIR_DATETIME).format(version.getModified()));
    resource.put("meta", meta);

    version.getContent().setValue(JsonUtil.toJson(resource));
  }

}
