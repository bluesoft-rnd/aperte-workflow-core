package pl.net.bluesoft.rnd.pt.ext.sched.event;

import org.quartz.Job;

import java.util.HashSet;
import java.util.Set;

/**
 * @author: amichalak@bluesoft.net.pl
 */
public abstract class CombinedJobSpecificEventListener extends JobSpecificEventListener<Job> {
    private Set<Class<? extends Job>> supportedJobs = new HashSet<Class<? extends Job>>();

    protected CombinedJobSpecificEventListener(Set<Class<? extends Job>> supportedJobs) {
        super(Job.class);
        this.supportedJobs = supportedJobs;
    }

    protected CombinedJobSpecificEventListener(final Class<? extends Job>... supportedJobs) {
        this(new HashSet<Class<? extends Job>>() {{
            for (Class<? extends Job> cls : supportedJobs) {
                add(cls);
            }
        }});
    }

    @Override
    protected boolean supports(Class<? extends Job> jobClass) {
        for (Class<? extends Job> cls : supportedJobs) {
            if (cls.isAssignableFrom(jobClass)) {
                return true;
            }
        }
        return false;
    }

}
