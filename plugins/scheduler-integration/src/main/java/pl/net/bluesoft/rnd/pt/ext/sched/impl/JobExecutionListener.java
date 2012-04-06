package pl.net.bluesoft.rnd.pt.ext.sched.impl;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.listeners.JobListenerSupport;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.pt.ext.sched.event.JobExecutedEvent;

import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author: amichalak@bluesoft.net.pl
 */
public class JobExecutionListener extends JobListenerSupport {
    private ProcessToolRegistry registry;

    private CopyOnWriteArraySet<Class<? extends Job>> silentJobsSet = new CopyOnWriteArraySet<Class<? extends Job>>();

    public JobExecutionListener(ProcessToolRegistry registry) {
        this.registry = registry;
    }

    @Override
    public String getName() {
        return JobExecutionListener.class.getName();
    }

    public void addSilentJobs(Class<? extends Job>... silentJobs) {
        silentJobsSet.addAll(Arrays.asList(silentJobs));
    }

    public void removeSilentJobs(Class<? extends Job>... silentJobs) {
        silentJobsSet.removeAll(Arrays.asList(silentJobs));
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        JobDetail jobDetail = context.getJobDetail();
        if (!silentJobsSet.contains(jobDetail.getJobClass())) {
            registry.getEventBusManager().post(new JobExecutedEvent(jobDetail.getJobClass(), jobDetail, jobException));
        }
    }
}
