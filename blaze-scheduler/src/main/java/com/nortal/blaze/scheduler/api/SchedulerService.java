package com.nortal.blaze.scheduler.api;

import com.nortal.blaze.scheduler.JobDao;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Date;

@Component(immediate = true, service = SchedulerService.class)
public class SchedulerService {
  @Reference
  private JobDao jobDao;

  public void schedule(String type, String identifier, Date scheduled) {
    jobDao.insert(type, identifier, scheduled);
  }

  public void reschedule(String type, String identifier, Date scheduled) {
    jobDao.cancel(type, identifier);
    jobDao.insert(type, identifier, scheduled);
  }

  public void unschedule(String type, String identifier) {
    jobDao.cancel(type, identifier);
  }

}
