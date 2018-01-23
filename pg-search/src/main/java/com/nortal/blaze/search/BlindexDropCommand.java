package com.nortal.blaze.search;

import com.nortal.blaze.search.dao.BlindexDao;
import com.nortal.blaze.search.model.Blindex;
import org.apache.commons.lang3.StringUtils;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.postgresql.util.PSQLException;

import java.util.List;

@Command(scope = "blindex", name = "drop-all", description = "drop all search indexes")
@Service
public class BlindexDropCommand implements Action {
  @Reference
  private BlindexDao blindexDao;

  @Override
  public Object execute() throws Exception {
    List<Blindex> indexes = blindexDao.load();
    for (Blindex blin : indexes) {
      String key = blin.getKey();
      try {
        System.out.println("dropping " + key);
        blindexDao.dropIndex(StringUtils.substringBefore(key, "."), StringUtils.substringAfter(key, "."));
      } catch (Exception e) {
        String err = e.getCause() instanceof PSQLException ? e.getCause().getMessage() : e.getMessage();
        System.err.println("failed " + key + ": " + err);
      }
    }
    return null;
  }

}
