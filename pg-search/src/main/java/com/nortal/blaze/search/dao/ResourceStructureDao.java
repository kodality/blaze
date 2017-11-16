package com.nortal.blaze.search.dao;

import com.nortal.blaze.search.model.StructureElement;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.springframework.jdbc.core.JdbcTemplate;

@Component(immediate = true, service = ResourceStructureDao.class)
public class ResourceStructureDao {
  @Reference
  private JdbcTemplate jdbcTemplate;

  public void create(List<StructureElement> elements) {
    String sql = "INSERT INTO resource_structure (path, element_types, is_many) VALUES (?,?::text[],?)";
    List<Object[]> args = new ArrayList<Object[]>(elements.size());
    elements.forEach(e -> args.add(arrrrg(e)));
    jdbcTemplate.batchUpdate(sql, args);
  }

  public void deleteAll() {
    jdbcTemplate.update("DELETE FROM resource_structure");
  }

  private static Object[] arrrrg(StructureElement def) {
    String types = "{" + StringUtils.join(def.getTypes(), ',') + "}";
    return new Object[] { def.getPath(), types, def.isMany() };
  }

}
