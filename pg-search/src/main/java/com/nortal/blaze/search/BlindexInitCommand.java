package com.nortal.blaze.search;

import com.nortal.blaze.core.service.conformance.ConformanceHolder;
import com.nortal.blaze.search.dao.BlindexDao;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.hl7.fhir.dstu3.model.SearchParameter;
import org.postgresql.util.PSQLException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.*;

@Command(scope = "blindex", name = "init", description = "init search indexes")
@Service
public class BlindexInitCommand implements Action {
  @Reference
  private BlindexDao blindexDao;

  @Override
  public Object execute() throws Exception {
    List<String> defined = ConformanceHolder.getDefinitions().stream().map(def -> def.getName()).collect(toList());
    if (CollectionUtils.isEmpty(defined)) {
      System.err.println("will not run. definitions either empty, either definitions not yet loaded.");
      return null;
    }
    Set<String> create = new HashSet<>();
    for (SearchParameter sp : ConformanceHolder.getSearchParams()) {
      if (sp.getExpression() == null) {
        continue;
      }
      List<String> exprs = Stream.of(split(sp.getExpression(), "|")).map((s) -> trim(s)).filter(s -> {
        return defined.contains(substringBefore(s, "."));
      }).collect(toList());
      create.addAll(exprs);
    }

    Set<String> current = blindexDao.load().stream().map(i -> i.getKey()).collect(Collectors.toSet());
    HashSet<String> ignore = new HashSet<>(create);
    ignore.retainAll(current);
    create.removeAll(ignore);
    current.removeAll(ignore);
    System.out.println("currently indexed: " + current + "\n");
    System.out.println("need to create: " + create + "\n");
    System.out.println("need to remove: " + current + "\n");
    save(create, current);
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
