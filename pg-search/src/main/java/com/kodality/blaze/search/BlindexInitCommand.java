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
 package com.kodality.blaze.search;

import com.kodality.blaze.core.service.conformance.ConformanceHolder;
import com.kodality.blaze.search.dao.BlindexDao;
import com.kodality.blaze.search.util.FhirPathHackUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.osgi.service.component.annotations.Activate;
import org.postgresql.util.PSQLException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.*;

@Command(scope = "blindex", name = "init", description = "init search indexes")
@Service
public class BlindexInitCommand implements Action {
  @Reference
  private BlindexDao blindexDao;

  @Activate
  public void init() throws Exception {
    execute();
  }

  @Override
  public Object execute() throws Exception {
    List<String> defined = ConformanceHolder.getDefinitions().stream().map(def -> def.getName()).collect(toList());
    if (CollectionUtils.isEmpty(defined)) {
      System.err.println("will not run. definitions either empty, either definitions not yet loaded.");
      return null;
    }
    Set<String> create =
        ConformanceHolder.getSearchParams().stream().filter(sp -> sp.getExpression() != null).flatMap(sp -> {
          return Stream.of(split(sp.getExpression(), "|"))
              .map((s) -> trim(s))
              .map(s -> FhirPathHackUtil.replaceAs(s))
              .filter(s -> defined.contains(substringBefore(s, ".")));
        }).collect(toSet());

    Set<String> current = blindexDao.load().stream().map(i -> i.getKey()).collect(Collectors.toSet());
    Set<String> drop = new HashSet<>(current);
    drop.removeAll(create);
    create.removeAll(current);
    System.out.println("currently indexed: " + current + "\n");
    System.out.println("need to create: " + create + "\n");
    System.out.println("need to remove: " + drop + "\n");
    save(create, drop);
    blindexDao.init();
    return null;
  }

  private void save(Set<String> create, Set<String> drop) {
    for (String key : create) {
      try {
        System.out.println("creating " + key);
        blindexDao.createIndex(substringBefore(key, "."), substringAfter(key, "."));
      } catch (Exception e) {
        String err = e.getMessage();
        if (e.getCause() instanceof PSQLException) {
          err = (e.getCause().getMessage().substring(0, e.getCause().getMessage().indexOf("\n")));
        }
        System.err.println("failed " + key + ": " + err);
      }
    }
    for (String key : drop) {
      try {
        System.out.println("dropping " + key);
        blindexDao.dropIndex(substringBefore(key, "."), substringAfter(key, "."));
      } catch (Exception e) {
        String err = e.getCause() instanceof PSQLException ? e.getCause().getMessage() : e.getMessage();
        System.err.println("failed " + key + ": " + err);
      }
    }
  }

}
