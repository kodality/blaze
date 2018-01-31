package com.nortal.blaze.core.service;

import com.nortal.blaze.core.exception.ServerException;
import com.nortal.blaze.core.iface.ResourceIndexer;
import com.nortal.blaze.core.iface.ResourceSaveHandler;
import com.nortal.blaze.core.iface.ResourceSearchHandler;
import com.nortal.blaze.core.iface.ResourceStorehouse;
import com.nortal.blaze.core.iface.ResourceValidator;
import com.nortal.blaze.core.model.ResourceContent;
import com.nortal.blaze.core.model.ResourceId;
import com.nortal.blaze.core.model.ResourceVersion;
import com.nortal.blaze.core.model.VersionId;
import com.nortal.blaze.core.model.search.SearchCriterion;
import com.nortal.blaze.core.model.search.SearchResult;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import java.util.ArrayList;
import java.util.List;

@Component(immediate = true, service = ResourceService.class)
public class ResourceService {
  @Reference(cardinality = ReferenceCardinality.OPTIONAL)
  private volatile ResourceStorehouse storehouse;
  @Reference(cardinality = ReferenceCardinality.OPTIONAL)
  private volatile ResourceSearchHandler searchHandler;
  @Reference
  private final List<ResourceIndexer> indexers = new ArrayList<>();
  @Reference(policy=ReferencePolicy.DYNAMIC)
  private final List<ResourceSaveHandler> handlers = new ArrayList<>();
  @Reference(policy=ReferencePolicy.DYNAMIC)
  private final List<ResourceValidator> validators = new ArrayList<>();

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

  public void delete(ResourceId id) {
    storehouse.delete(id);
    indexers.forEach(i -> i.delete(id));
  }

  public ResourceVersion load(VersionId id) {
    return storehouse.load(id);
  }

  public List<ResourceVersion> loadHistory(ResourceId id) {
    return storehouse.loadHistory(id);
  }

  public SearchResult search(SearchCriterion criteria) {
    if (searchHandler == null) {
      throw new ServerException("search not installed");
    }
    SearchResult result = searchHandler.search(criteria);
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
    this.indexers.add(indexer);
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
