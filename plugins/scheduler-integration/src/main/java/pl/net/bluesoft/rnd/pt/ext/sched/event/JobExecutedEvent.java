package pl.net.bluesoft.rnd.pt.ext.sched.event;

import org.quartz.Job;
import org.quartz.JobDetail;

import java.io.Serializable;

/**
 * @author: amichalak@bluesoft.net.pl
 */
public class JobExecutedEvent implements Serializable {
    protected Class<? extends Job> jobClass;
    protected JobDetail jobDetail;
    protected boolean failed;
    protected Exception exception;

    public JobExecutedEvent(Class<? extends Job> jobClass, JobDetail jobDetail, Exception exception) {
        this.jobClass = jobClass;
        this.jobDetail = jobDetail;
        this.failed = exception != null;
        this.exception = exception;
    }

    public Class<? extends Job> getJobClass() {
        return jobClass;
    }

    public JobDetail getJobDetail() {
        return jobDetail;
    }

    public boolean hasFailed() {
        return failed;
    }

    public Exception getException() {
        return exception;
    }
}
