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

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.SQLException;

@Component(immediate = true, service = { FhirDataSource.class,
                                         DataSource.class }, configurationPid = "com.kodality.blaze.pg")
public class FhirDataSource extends SimpleDataSource {
  // @Reference
  // private ClientIdentity clientIdentity;

  @Override
  public Connection getConnection() throws SQLException {
    Connection c = super.getConnection();
    // TODO: maybe next time
    // String sql = "SELECT core.set_user(?::jsonb)";
    // try (PreparedStatement ps = c.prepareStatement(sql)) {
    // String json = new Gson().toJson(clientIdentity.get());
    // ps.setString(1, json);
    // ps.execute();
    // }
    return c;
  }

}
