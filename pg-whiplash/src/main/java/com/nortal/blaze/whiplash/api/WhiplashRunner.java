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
 package com.nortal.blaze.whiplash.api;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import javax.sql.DataSource;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.AbstractResourceAccessor;
import org.apache.commons.lang3.StringUtils;
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
    JdbcConnection connection = new JdbcConnection(getDataSource().getConnection());
    try {
      Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);
      database.setDefaultSchemaName("public");
      Liquibase liquibase = new Liquibase(changelogFile, new BundleResourceAccessor(), database);
      liquibase.update(new Contexts(context));
    } finally {
      connection.close();
    }
  }

  private class BundleResourceAccessor extends AbstractResourceAccessor {

    @Override
    public Set<InputStream> getResourcesAsStream(String path) throws IOException {
      path = StringUtils.replacePattern(path, "[^/]*/../", "");
      URL url = path.startsWith("bundle:") ? new URL(path) : getBundleContext().getBundle().getResource(path);
      if(url == null){
        throw new IllegalStateException(path + " not found");
      }
      URLConnection con = url.openConnection();
      return Collections.singleton(con.getInputStream());
    }

    @Override
    public Set<String> list(String relativeTo,
                            String path,
                            boolean includeFiles,
                            boolean includeDirectories,
                            boolean recursive) throws IOException {
      if (StringUtils.isNotEmpty(relativeTo)) {
        path = StringUtils.substringBeforeLast(relativeTo, "/") + "/" + path;
      }
      Enumeration<URL> urls = getBundleContext().getBundle().findEntries(path, "*.*", recursive);
      if (urls == null) {
        throw new FileNotFoundException(path);
      }
      Set<String> result = new HashSet<>();
      while (urls.hasMoreElements()) {
        result.add(urls.nextElement().getFile());
      }
      return result;
    }

    @Override
    public ClassLoader toClassLoader() {
      return this.getClass().getClassLoader();
    }

  }

}
