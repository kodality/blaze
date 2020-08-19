package com.kodality.blaze.scheduler;

import com.kodality.blaze.util.sql.FhirJdbcTemplate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Date;
import java.util.List;

@Component(immediate = true, service = JobDao.class)
public class JobDao {
  @Reference
  private FhirJdbcTemplate jdbcTemplate;

  public void insert(String type, String identifier, Date scheduled) {
    String sql = "INSERT INTO scheduler.job (type, identifier, scheduled) values (?,?,?)";
    jdbcTemplate.update(sql, type, identifier, scheduled);
  }

  public void cancel(String type, String identifier) {
//    String sql = "UPDATE scheduler.job SET status = 'cancelled' where type = ? and identifier = ? and status = 'active'";
    String sql = "DELETE FROM scheduler.job where type = ? and identifier = ? and status = 'active'";
    jdbcTemplate.update(sql, type, identifier);
  }

  public boolean lock(Long id) {
    String sql = "UPDATE scheduler.job SET started = now() where id = ? and started is null and status = 'active'";
    return jdbcTemplate.update(sql, id) > 0;
  }

  public void finish(Long id, String log) {
    String sql = "UPDATE scheduler.job SET finished = now(), status = 'finished', log = ? where id = ?";
    jdbcTemplate.update(sql, log, id);
  }

  public void fail(Long id, String log) {
    String sql = "UPDATE scheduler.job SET finished = now(), status = 'failed', log = ? where id = ?";
    jdbcTemplate.update(sql, log, id);
  }

  public List<SchedulerJob> getExecutables() {
    String sql =
        "SELECT id, type, identifier FROM scheduler.job WHERE started is null and status = 'active' and scheduled <= now()";
    return jdbcTemplate.query(sql, (rs, i) -> {
      return new SchedulerJob(rs.getLong(1), rs.getString(2), rs.getString(3));
    });
  }

}
