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
 package com.kodality.blaze.whiplash.api;

import java.sql.SQLException;
import javax.sql.DataSource;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.OSGiResourceAccessor;
import org.osgi.framework.BundleContext;

public abstract class WhiplashRunner {
  private final String changelogFile;

  public WhiplashRunner(String changelogFile) {
    this.changelogFile = changelogFile;
  }

  protected abstract DataSource getDataSource();

  protected abstract BundleContext getBundleContext();

  public String getName() {
    return getBundleContext().getBundle().getHeaders().get("Bundle-Name");
  }

  public void run() throws SQLException, LiquibaseException {
    run(null);
  }

  public void run(String context) throws SQLException, LiquibaseException {
    try (JdbcConnection connection = new JdbcConnection(getDataSource().getConnection())) {
      Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);
      database.setDefaultSchemaName("public");
      try (OSGiResourceAccessor accessor = new OSGiResourceAccessor(getBundleContext().getBundle())) {
        Liquibase liquibase = new Liquibase(changelogFile, accessor, database);
        liquibase.update(new Contexts(context));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

}
