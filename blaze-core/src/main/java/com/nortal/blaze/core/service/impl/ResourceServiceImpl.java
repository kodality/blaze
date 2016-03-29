package com.nortal.blaze.core.service.impl;

import com.nortal.blaze.core.exception.ServerException;
import com.nortal.blaze.core.iface.Google;
import com.nortal.blaze.core.iface.ResourceIndexer;
import com.nortal.blaze.core.iface.ResourceSaveHandler;
import com.nortal.blaze.core.iface.ResourceStorehouse;
import com.nortal.blaze.core.iface.ResourceValidator;
import com.nortal.blaze.core.model.ResourceContent;
import com.nortal.blaze.core.model.ResourceId;
import com.nortal.blaze.core.model.ResourceVersion;
import com.nortal.blaze.core.model.VersionId;
import com.nortal.blaze.core.model.search.SearchCriterion;
import com.nortal.blaze.core.model.search.SearchResult;
import com.nortal.blaze.core.service.ResourceService;
import java.util.ArrayList;
import java.util.List;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;

@Component(immediate = true)
@Service(ResourceService.class)
public class ResourceServiceImpl implements ResourceService {
  @Reference(cardinality = ReferenceCardinality.OPTIONAL_UNARY, policy = ReferencePolicy.DYNAMIC)
  private volatile ResourceStorehouse storehouse;
  @Reference(cardinality = ReferenceCardinality.OPTIONAL_UNARY, policy = ReferencePolicy.DYNAMIC)
  private volatile Google google;

  @Reference(cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, referenceInterface = ResourceIndexer.class, policy = ReferencePolicy.DYNAMIC)
  private final List<ResourceIndexer> indexers = new ArrayList<>();
  @Reference(cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, referenceInterface = ResourceSaveHandler.class, policy = ReferencePolicy.DYNAMIC)
  private final List<ResourceSaveHandler> handlers = new ArrayList<>();
  @Reference(cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, referenceInterface = ResourceValidator.class, policy = ReferencePolicy.DYNAMIC)
  private final List<ResourceValidator> validators = new ArrayList<>();

  @Override
  public ResourceVersion save(VersionId id, ResourceContent input) {
    validators.forEach(v -> v.validate(id.getResourceType(), input));
    ResourceContent content = beforeSave(id, input);
    ResourceVersion version = storehouse.save(id, content);
    indexers.forEach(i -> i.index(version));
    handlers.forEach(h -> h.afterSave(version));
    return version;
  }

  private ResourceContent beforeSave(VersionId id, ResourceContent content) {
    for (ResourceSaveHandler handler : handlers) {
      content = handler.beforeSave(id, content);
    }
    return content;
  }

  @Override
  public void delete(ResourceId id) {
    storehouse.delete(id);
    indexers.forEach(i -> i.delete(id));
  }

  @Override
  public ResourceVersion load(VersionId id) {
    return storehouse.load(id);
  }

  @Override
  public List<ResourceVersion> loadHistory(ResourceId id) {
    return storehouse.loadHistory(id);
  }

  @Override
  public SearchResult search(SearchCriterion criteria) {
    if (google == null) {
      throw new ServerException("search not installed");
    }
    SearchResult result = google.search(criteria);
    if (result.isEmpty()) {
      return result;
    }
    for (ResourceVersion entry : result.getEntries()) {
      if (entry.getContent() == null) {
        entry.setContent(load(entry.getId()).getContent());
      }
    }
    return result;
  }

  protected void bind(ResourceIndexer indexer) {
    indexers.add(indexer);
  }

  protected void bind(ResourceSaveHandler handler) {
    handlers.add(handler);
  }

  protected void bind(ResourceValidator validator) {
    validators.add(validator);
  }

  protected void unbind(ResourceIndexer indexer) {
    indexers.remove(indexer);
  }

  protected void unbind(ResourceSaveHandler handler) {
    handlers.remove(handler);
  }

  protected void unbind(ResourceValidator validator) {
    validators.remove(validator);
  }

}
