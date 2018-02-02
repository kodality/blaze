package com.nortal.fhir.rest.feature;

import com.nortal.blaze.core.api.ResourceSaveHandler;
import com.nortal.blaze.core.exception.FhirException;
import com.nortal.blaze.core.model.ResourceContent;
import com.nortal.blaze.core.model.ResourceVersion;
import com.nortal.blaze.core.model.VersionId;
import com.nortal.blaze.core.model.search.SearchCriterion;
import com.nortal.blaze.core.model.search.SearchResult;
import com.nortal.blaze.core.service.ResourceService;
import com.nortal.blaze.fhir.structure.service.ResourceRepresentationService;
import com.nortal.fhir.rest.util.SearchUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Resource;
import org.osgi.service.component.annotations.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * https://www.hl7.org/fhir/http.html#transaction 
 * Conditional Reference implementation. 
 * originally should only apply to transaction, but we thought might be global..
 * 
 * TODO: move somewhere more appropriate
 */
@Component(immediate = true, service = ResourceSaveHandler.class)
public class ResourceConditionalReferenceFeature implements ResourceSaveHandler {
  @org.osgi.service.component.annotations.Reference
  private ResourceRepresentationService representationService;
  @org.osgi.service.component.annotations.Reference
  private ResourceService resourceService;

  @Override
  public ResourceContent beforeSave(VersionId id, ResourceContent content) {
    Resource resource = representationService.parse(content.getValue());
    findProperties(resource, new HashSet<>(), Reference.class).forEach(reference -> {
      String uri = reference.getReference();
      if (!uri.contains("?")) {
        return;
      }
      String resourceType = StringUtils.substringBefore(uri, "?");
      String query = StringUtils.substringAfter(uri, "?");

      SearchCriterion criteria = new SearchCriterion(resourceType, SearchUtil.parse(query, resourceType));
      criteria.getResultParams().clear();
      SearchResult result = resourceService.search(criteria);
      if (result.getTotal() != 1) {
        throw new FhirException(400, "found more than one resources by " + uri);
      }
      reference.setReference(result.getEntries().get(0).getId().getResourceReference());
    });
    String newContent = representationService.compose(resource, content.getContentType());
    content.setValue(newContent);
    return content;
  }

  @Override
  public void afterSave(ResourceVersion savedVersion) {
    // nothing
  }

  @SuppressWarnings("unchecked")
  private <T> Stream<T> findProperties(Object object, Set<Object> exclude, Class<T> fieldClazz) {
    if (object == null) {
      return Stream.empty();
    }
    // if (fieldClazz.equals(object.getClass())) {
    //   return Stream.of((T) object);
    // }

    Field[] fields = FieldUtils.getAllFields(object.getClass());
    return Stream.of(fields).flatMap(field -> {
      field.setAccessible(true);
      Object obj = getFieldValue(field, object);
      if (obj == null) {
        return Stream.empty();
      }
      if (exclude.contains(obj)) {
        return Stream.empty();
      }
      exclude.add(obj);
      if (fieldClazz.equals(field.getType())) {
        return Stream.of((T) obj);
      }
      if (Type.class.equals(field.getType()) && fieldClazz.equals(obj.getClass())) {
        return Stream.of((T) obj);
      }
      if (obj instanceof Collection) {
        return ((Collection<?>) obj).stream().flatMap(o -> findProperties(o, exclude, fieldClazz));
      }
      return findProperties(obj, exclude, fieldClazz);
    });
  }

  private Object getFieldValue(Field field, Object from) {
    try {
      return field.get(from);
    } catch (IllegalArgumentException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

}
