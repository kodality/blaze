package com.nortal.blaze.search;

import com.nortal.blaze.search.dao.BlindexDao;
import com.nortal.blaze.search.model.Blindex;
import com.nortal.fhir.conformance.content.ResourceDefinitionsMonitor;
import com.nortal.fhir.conformance.operations.SearchParameterMonitor;
import org.apache.commons.lang3.StringUtils;
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

@Command(scope = "blindex", name = "init", description = "init search indexes")
@Service
public class BlindexInitCommand implements Action {
  @Reference
  private BlindexDao blindexDao;

  @Override
  public Object execute() throws Exception {
    List<Blindex> indexes = blindexDao.load();
    Set<String> current = indexes.stream().map(i -> i.getKey()).collect(Collectors.toSet());
    System.out.println("currently indexed: " + current + "\n");
    Set<String> create = new HashSet<>();
    List<String> defined = ResourceDefinitionsMonitor.get().stream().map(def -> def.getName()).collect(toList());
    for (SearchParameter sp : SearchParameterMonitor.get()) {
      if (sp.getExpression() == null) {
        continue;
      }
      List<String> exprs =
          Stream.of(StringUtils.split(sp.getExpression(), "|")).map((s) -> StringUtils.trim(s)).filter(s -> {
            return defined.contains(StringUtils.substringBefore(s, "."));
          }).collect(toList());
      create.addAll(exprs);
    }
    HashSet<String> ignore = new HashSet<>(create);
    ignore.retainAll(current);
    create.removeAll(ignore);
    current.removeAll(ignore);
    System.out.println("need to create: " + create + "\n");
    System.out.println("need to remove: " + current + "\n");
    save(create, current);
    return null;
  }

  private void save(Set<String> create, Set<String> drop) {
    for (String key : create) {
      try {
        System.out.println("creating " + key);
        blindexDao.createIndex(StringUtils.substringBefore(key, "."), StringUtils.substringAfter(key, "."));
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
        blindexDao.dropIndex(StringUtils.substringBefore(key, "."), StringUtils.substringAfter(key, "."));
      } catch (Exception e) {
        String err = e.getCause() instanceof PSQLException ? e.getCause().getMessage() : e.getMessage();
        System.err.println("failed " + key + ": " + err);
      }
    }
  }

}