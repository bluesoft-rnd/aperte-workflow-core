package pl.net.bluesoft.rnd.pt.ext.sched.event;

import org.quartz.Job;
import pl.net.bluesoft.util.eventbus.EventListener;

import java.lang.reflect.ParameterizedType;

/**
 * @author: amichalak@bluesoft.net.pl
 */
public abstract class JobSpecificEventListener<T extends Job> implements EventListener<JobExecutedEvent> {
    protected Class<T> jobClass;

    protected JobSpecificEventListener() {
        this.jobClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    protected JobSpecificEventListener(Class<T> jobClass) {
        this.jobClass = jobClass;
    }

    protected boolean supports(Class<? extends Job> jobClass) {
        return jobClass.isAssignableFrom(jobClass);
    }
    
    @Override
    public void onEvent(JobExecutedEvent jobExecutedEvent) {
        if (supports(jobExecutedEvent.getJobClass())) {
            handleEvent(jobExecutedEvent);
        }
    }

    public abstract void handleEvent(JobExecutedEvent jobExecutedEvent);
}
