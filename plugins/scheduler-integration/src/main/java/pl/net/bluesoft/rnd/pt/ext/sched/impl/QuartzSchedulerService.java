package pl.net.bluesoft.rnd.pt.ext.sched.impl;

import org.hibernate.Session;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.calendar.HolidayCalendar;
import org.quartz.impl.matchers.EverythingMatcher;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.utils.Key;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ReturningProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.pt.ext.sched.event.ScheduleJobEvent;
import pl.net.bluesoft.rnd.pt.ext.sched.model.SchedulerProperty;
import pl.net.bluesoft.rnd.pt.ext.sched.service.ProcessToolSchedulerService;
import pl.net.bluesoft.rnd.pt.ext.sched.service.SchedulerServiceInternalError;
import pl.net.bluesoft.util.eventbus.EventListener;
import pl.net.bluesoft.util.lang.Formats;
import pl.net.bluesoft.util.lang.Predicate;
import pl.net.bluesoft.util.lang.Strings;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author: amichalak@bluesoft.net.pl
 */
public class QuartzSchedulerService implements ProcessToolSchedulerService, EventListener<ScheduleJobEvent> {
    private static final Logger logger = Logger.getLogger(QuartzSchedulerService.class.getName());

    private ProcessToolRegistry registry;
    private Scheduler scheduler;

    private Properties schedulerProperties;
    private JobExecutionListener jobListener;

    public QuartzSchedulerService(ProcessToolRegistry registry, Properties fileSchedulerProperties) {
        this.registry = registry;
        this.schedulerProperties = mergeProperties(fileSchedulerProperties, loadPersistentProperties());
        this.jobListener = new JobExecutionListener(registry);
    }

    public void init() throws SchedulerException {
        logger.warning("Starting scheduler service...");
        scheduler = new StdSchedulerFactory(schedulerProperties).getScheduler();
        scheduler.addCalendar(QuartzSchedulerService.class.getName(), new HolidayCalendar(), true, true);
        scheduler.getListenerManager().addJobListener(jobListener, EverythingMatcher.allJobs());
        scheduler.start();
        logger.warning("Scheduler service online!");
    }

    public void destroy() throws SchedulerException {
        logger.warning("Stopping scheduler service...");
        scheduler.clear();
        scheduler.shutdown();
        logger.warning("Scheduler service offline!");
    }

    private Properties mergeProperties(Properties fileSchedulerProperties, Properties persistentProperties) {
        Properties mergedProperties = new Properties(persistentProperties);
        for (String key : fileSchedulerProperties.stringPropertyNames()) {
            String value = mergedProperties.getProperty(key);
            if (value == null) {
                mergedProperties.put(key, fileSchedulerProperties.getProperty(key));
            }
        }
        return mergedProperties;
    }

    private Properties loadPersistentProperties() {
        return registry.withProcessToolContext(new ReturningProcessToolContextCallback<Properties>() {
            @Override
            public Properties processWithContext(ProcessToolContext ctx) {
                Properties properties = new Properties();
                Session session = ctx.getHibernateSession();
                List<SchedulerProperty> list = session.createCriteria(SchedulerProperty.class).list();
                for (SchedulerProperty prop : list) {
                    properties.put(prop.getName(), prop.getValue());
                }
                return properties;
            }
        });
    }

    @Override
    public void enableJobQuietMode(final Class<? extends Job>... jobClasses) {
        logger.info("Enabled quiet mode for jobs: " + Formats.joinClassNames(jobClasses));
        jobListener.addSilentJobs(jobClasses);
    }

    @Override
    public void disableJobQuietMode(Class<? extends Job>... jobClasses) {
        logger.info("Disabled quiet mode for jobs: " + Formats.joinClassNames(jobClasses));
        jobListener.removeSilentJobs(jobClasses);
    }

    @Override
    public void scheduleJob(final JobDetail jobDetail, final Trigger... triggers) {
        final StringBuilder sb = new StringBuilder();
        final List<Trigger> list = new ArrayList<Trigger>() {{
            for (int i = 0; i < triggers.length; ++i) {
                Trigger t = triggers[i];
                add(t);
                sb.append("[").append(t.getStartTime()).append("]");
                if (i != triggers.length - 1) {
                    sb.append(", ");
                }
            }
        }};
        Map<JobDetail, List<Trigger>> jobMap = new HashMap<JobDetail, List<Trigger>>() {{
            put(jobDetail, list);
        }};
        logger.info("Scheduling job on: " + sb + " of type: " + jobDetail.getJobClass().getName());
        try {
            scheduler.scheduleJobs(jobMap, true);
        }
        catch (SchedulerException e) {
            logger.log(Level.SEVERE, "Error while scheduling job: " + e.getMessage(), e);
        }
    }

    @Override
    public void cancelScheduledJobs(String jobGroupName, final Class<? extends Job>... jobClasses) {
        cancelJobGroupInternal(jobGroupName, new Predicate<JobDetail>() {
            Set<String> classNames = new HashSet<String>(jobClasses.length) {{
                for (Class<? extends Job> clazz : jobClasses) {
                    add(clazz.getName());
                }
            }};

            @Override
            public boolean apply(JobDetail jobDetail) {
                return classNames.contains(jobDetail.getJobClass().getName());
            }
        });
    }

    @Override
    public void cancelScheduledJobGroup(String jobGroupName) {
        cancelJobGroupInternal(jobGroupName, new Predicate<JobDetail>() {
            @Override
            public boolean apply(JobDetail jobDetail) {
                return true;
            }
        });
    }

    private void cancelJobGroupInternal(String jobGroupName, Predicate<JobDetail> predicate) {
        try {
            if (!Strings.hasLength(jobGroupName)) {
                jobGroupName = Key.DEFAULT_GROUP;
            }
            List<TriggerKey> cancelledTriggerKeys = new ArrayList<TriggerKey>();
            Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(jobGroupName));
            for (JobKey jobKey : jobKeys) {
                JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                if (predicate.apply(jobDetail)) {
                    List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                    for (Trigger trigger : triggers) {
                        cancelledTriggerKeys.add(trigger.getKey());
                    }
                }
            }
            logger.warning("Cancelling " + cancelledTriggerKeys.size() + " jobs for job group: " + jobGroupName);
            scheduler.unscheduleJobs(cancelledTriggerKeys);
        }
        catch (SchedulerException e) {
            throw new SchedulerServiceInternalError(e);
        }
    }

    @Override
    public void onEvent(ScheduleJobEvent scheduleJobEvent) {
        scheduleJob(scheduleJobEvent.getJobDetail(), scheduleJobEvent.getTrigger());
    }
}
