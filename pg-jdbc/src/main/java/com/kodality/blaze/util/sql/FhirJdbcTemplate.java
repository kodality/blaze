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
 package com.kodality.blaze.util.sql;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.springframework.jdbc.core.JdbcTemplate;

@Component(immediate = true, service = { FhirJdbcTemplate.class, JdbcTemplate.class })
public class FhirJdbcTemplate extends JdbcTemplate {
  @Reference(name = "dataSource")
  protected void bind(FhirDataSource ds) {
    setDataSource(ds);
  }

  protected void unbind(FhirDataSource ds) {
    // boo
  }
}
