package com.nortal.blaze.store;

import com.nortal.blaze.core.exception.FhirException;
import com.nortal.blaze.core.exception.FhirNotFoundException;
import com.nortal.blaze.core.iface.ResourceStorehouse;
import com.nortal.blaze.core.model.ResourceContent;
import com.nortal.blaze.core.model.ResourceId;
import com.nortal.blaze.core.model.ResourceVersion;
import com.nortal.blaze.core.model.VersionId;
import com.nortal.blaze.representation.ResourceComposer;
import com.nortal.blaze.representation.ResourceParser;
import com.nortal.blaze.store.dao.ResourceDao;
import java.util.List;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

@Component(immediate = true)
@Service(ResourceStorehouse.class)
public class PostgreStorehouse implements ResourceStorehouse {
  @Reference
  private ResourceDao resourceDao;

  @Override
  public ResourceVersion save(VersionId id, ResourceContent content) {
    if (!content.getContentType().contains("json")) {
      content.setValue(ResourceComposer.compose(ResourceParser.parse(content.getValue()), "json"));
    }
    ResourceVersion version = new ResourceVersion(id, content);
    version.getId().setVersion(resourceDao.getLastVersion(id) + 1);
    resourceDao.create(version);
    return version;
  }

  @Override
  public void delete(ResourceId id) {
    resourceDao.delete(id);
  }

  @Override
  public ResourceVersion load(VersionId id) {
    ResourceVersion version = resourceDao.load(id);
    if (version == null) {
      throw new FhirNotFoundException(id.getReference() + " not found");
    }
    return version;
  }

  @Override
  public List<ResourceVersion> loadHistory(ResourceId id) {
    throw new FhirException(501, "soon");
  }

}
