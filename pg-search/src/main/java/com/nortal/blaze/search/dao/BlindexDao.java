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
 package com.nortal.blaze.search.dao;

import com.nortal.blaze.core.exception.FhirServerException;
import com.nortal.blaze.search.model.Blindex;
import com.nortal.blaze.util.sql.SqlBuilder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(immediate = true, service = BlindexDao.class)
public class BlindexDao {
  private static final Map<String, String> parasols = new HashMap<String, String>();
  @Reference
  private JdbcTemplate jdbcTemplate;

  @Activate
  public void init() {
    parasols.clear();
    load(Blindex.PARASOL).forEach(p -> parasols.put(p.getKey(), p.getName()));
  }

  public static String getParasol(String resourceType, String path) {
    String key = resourceType + "." + path;
    if (!parasols.containsKey(key)) {
      throw new FhirServerException(500, key + " not indexed");
    }
    return parasols.get(key);
  }

  public List<Blindex> load() {
    return load(null);
  }

  public List<Blindex> load(String type) {
    SqlBuilder sb = new SqlBuilder();
    sb.append("SELECT resource_type, path, index_name FROM blindex");
    sb.appendIfNotNull("WHERE index_type = ?", type);
    return jdbcTemplate.query(sb.getSql(), sb.getParams(), new ParasolRowMapper());
  }

  public void createIndex(String resourceType, String path) {
    jdbcTemplate.queryForObject("SELECT create_blindex(?,?)", String.class, resourceType, path);
  }

  public void dropIndex(String resourceType, String path) {
    jdbcTemplate.queryForObject("SELECT drop_blindex(?,?)", String.class, resourceType, path);
  }

  private static class ParasolRowMapper implements RowMapper<Blindex> {

    @Override
    public Blindex mapRow(ResultSet rs, int rowNum) throws SQLException {
      Blindex p = new Blindex();
      p.setResourceType(rs.getString("resource_type"));
      p.setPath(rs.getString("path"));
      p.setName(rs.getString("index_name"));
      return p;
    }

  }

}
