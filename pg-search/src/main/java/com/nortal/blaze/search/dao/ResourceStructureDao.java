package com.nortal.blaze.search.dao;

import com.nortal.blaze.search.model.StructureElement;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.jdbc.core.JdbcTemplate;

@Component(immediate = true)
@Service(ResourceStructureDao.class)
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
