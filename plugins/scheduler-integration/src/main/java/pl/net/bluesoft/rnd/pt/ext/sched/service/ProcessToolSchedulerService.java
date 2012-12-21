package pl.net.bluesoft.rnd.pt.ext.sched.service;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Trigger;

/**
 * @author: amichalak@bluesoft.net.pl
 */
public interface ProcessToolSchedulerService {
    void scheduleJob(JobDetail jobDetail, Trigger... triggers);
    void cancelScheduledJobs(String jobGroupName, final Class<? extends Job>... jobClasses);
    void cancelScheduledJobGroup(String jobGroupName);
    void enableJobQuietMode(Class<? extends Job>... jobClasses);
    void disableJobQuietMode(Class<? extends Job>... jobClasses);
}
