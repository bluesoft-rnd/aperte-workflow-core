package pl.net.bluesoft.rnd.pt.ext.sched.event;

import org.quartz.JobDetail;
import org.quartz.Trigger;

import java.io.Serializable;

/**
 * @author: amichalak@bluesoft.net.pl
 */
public class ScheduleJobEvent implements Serializable {
    protected JobDetail jobDetail;
    protected Trigger trigger;

    public ScheduleJobEvent(JobDetail jobDetail, Trigger trigger) {
        this.jobDetail = jobDetail;
        this.trigger = trigger;
    }

    public JobDetail getJobDetail() {
        return jobDetail;
    }

    public Trigger getTrigger() {
        return trigger;
    }
}
