package com.nortal.blaze.scheduler;

import com.nortal.blaze.core.util.Osgi;
import com.nortal.blaze.scheduler.api.ScheduleJobRunner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.karaf.scheduler.Job;
import org.apache.karaf.scheduler.JobContext;
import org.apache.karaf.scheduler.Scheduler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;

@Slf4j
@Component(immediate = true, property = { Scheduler.PROPERTY_SCHEDULER_EXPRESSION + "=0 0/5 * * * ?",
                                          Scheduler.PROPERTY_SCHEDULER_NAME + "=blaze-scheduler" })
public class SchedulerJobRunner implements Job {
  @Reference
  private JobDao jobDao;

  @Override
  public void execute(JobContext ctx) {
    log.info("starting scheduler job runner");
    List<SchedulerJob> jobs = jobDao.getExecutables();
    log.info("found " + jobs.size() + " jobs");
    jobs.stream().forEach(job -> {
      if (!jobDao.lock(job.getId())) {
        log.info("could not lock " + job.getId() + ", continuing");
        return;
      }
      try {
        Osgi.getBeans(ScheduleJobRunner.class)
            .stream()
            .filter(r -> r.getType().equals(job.getType()))
            .findFirst()
            .ifPresent(runner -> {
              String log = runner.run(job.getIdentifier());
              jobDao.finish(job.getId(), log);
            });
        //TODO: think what to do if no runners found. they might be on other node!
      } catch (Throwable e) {
        jobDao.fail(job.getId(), ExceptionUtils.getStackTrace(e));
        log.error("error during job " + job.getId() + "execution: ", e);
      }
    });
  }

}
