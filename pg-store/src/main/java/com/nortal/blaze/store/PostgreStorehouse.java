package com.nortal.blaze.store;

import com.nortal.blaze.auth.ClientIdentity;
import com.nortal.blaze.core.exception.FhirException;
import com.nortal.blaze.core.exception.FhirNotFoundException;
import com.nortal.blaze.core.iface.ResourceStorehouse;
import com.nortal.blaze.core.model.ResourceContent;
import com.nortal.blaze.core.model.ResourceId;
import com.nortal.blaze.core.model.ResourceVersion;
import com.nortal.blaze.core.model.VersionId;
import com.nortal.blaze.core.util.JsonUtil;
import com.nortal.blaze.fhir.structure.api.ResourceComposer;
import com.nortal.blaze.store.dao.ResourceDao;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.Map;

@Component(immediate = true, service = ResourceStorehouse.class)
public class PostgreStorehouse implements ResourceStorehouse {
  @Reference
  private ClientIdentity clientIdentity;
  @Reference
  private ResourceDao resourceDao;

  @Override
  public ResourceVersion save(VersionId id, ResourceContent content) {
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
  }

  @Override
  public void delete(ResourceId id) {
    ResourceVersion current = resourceDao.load(new VersionId(id));
    if (current.isDeleted()) {
      return;
    }
    ResourceVersion version = new ResourceVersion(new VersionId(id), new ResourceContent("{}", "json"));
    version.setDeleted(true);
    version.getId().setVersion(resourceDao.getLastVersion(id) + 1);
    if (clientIdentity.get() != null) {
      version.setAuthor(clientIdentity.get().getClaims());
    }
    resourceDao.create(version);
  }

  @Override
  public ResourceVersion load(VersionId id) {
    ResourceVersion version = resourceDao.load(id);
    if (version == null) {
      throw new FhirNotFoundException(id.getReference() + " not found");
    }
    if (id.getVersion() == null && version.isDeleted()) {
      throw new FhirException(410, "resource deleted");
    }
    fixId(version);

    return version;
  }

  @Override
  public List<ResourceVersion> loadHistory(ResourceId id) {
    List<ResourceVersion> history = resourceDao.loadHistory(id);
    history.forEach(this::fixId);
    return history;
  }

  private void fixId(ResourceVersion version) {
    // TODO: maybe rewrite this when better times come and resource will be parsed until end.
    Map<String, Object> hack = JsonUtil.fromJson(version.getContent().getValue());
    hack.put("id", version.getId().getResourceId());
    version.getContent().setValue(JsonUtil.toJson(hack));
  }

}
