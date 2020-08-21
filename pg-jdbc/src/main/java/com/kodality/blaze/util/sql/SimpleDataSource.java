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

import org.apache.commons.dbcp2.BasicDataSource;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

@Component(immediate = true, service = { SimpleDataSource.class }, configurationPid = "com.kodality.blaze.pg")
public class SimpleDataSource extends BasicDataSource {

  @Activate
  void activate(Map<String, String> props) {
    updated(props);
  }

  @Modified
  public void updated(Map<String, String> props) {
    setDriverClassLoader(this.getClass().getClassLoader());
    setDriverClassName("org.postgresql.Driver");
    setInitialSize(5);
    setValidationQuery("select 1");
    setConnectionInitSqls(Collections.singleton("set search_path to fhir,core,public"));

    setMaxTotal(Integer.valueOf(props.get("db.maxActive")));
    setUrl(props.get("db.url"));
    setUsername(props.get("db.username"));
    setPassword(props.get("db.password"));

    try {
      createDataSource();
    } catch (SQLException e) {
      System.out.println("woo");
    }
  }

}
