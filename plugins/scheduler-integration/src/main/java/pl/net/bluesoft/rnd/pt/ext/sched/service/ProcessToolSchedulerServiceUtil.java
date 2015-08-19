package pl.net.bluesoft.rnd.pt.ext.sched.service;

import org.quartz.*;

import static org.quartz.CronScheduleBuilder.*;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;

/**
 * User: POlszewski
 * Date: 2014-05-20
 */
public class ProcessToolSchedulerServiceUtil {
	public static void schedule(Class<? extends Job> clazz, int interval) {
		schedule(clazz, interval, new JobDataMap());
	}

	public static void schedule(Class<? extends Job> clazz, int interval, JobDataMap dataMap) {
		schedule(clazz, simpleSchedule().withIntervalInSeconds(interval).repeatForever(), dataMap);
	}

	public static void schedule(Class<? extends Job> clazz, String cron) {
		schedule(clazz, cron, new JobDataMap());
	}

	public static void schedule(Class<? extends Job> clazz, String cron, JobDataMap dataMap) {
		schedule(clazz, cronSchedule(cron), dataMap);
	}

	private static void schedule(Class<? extends Job> clazz, ScheduleBuilder scheduleBuilder, JobDataMap dataMap) {
		ProcessToolSchedulerService service = getRegistry().getRegisteredService(ProcessToolSchedulerService.class);

		String identity = "job";

		JobDetail jobDetail = JobBuilder.newJob(clazz)
				.withIdentity(identity, clazz.getName())
				.usingJobData(dataMap)
				.build();

		Trigger trigger = TriggerBuilder.newTrigger()
				.withIdentity(identity, clazz.getName())
				.withSchedule(scheduleBuilder)
				.forJob(jobDetail)
				.build();

		service.scheduleJob(jobDetail, trigger);
	}

	public static void unschedule(Class<? extends Job> clazz) {
		ProcessToolSchedulerService service = getRegistry().getRegisteredService(ProcessToolSchedulerService.class);

		service.cancelScheduledJobGroup(clazz.getName());
	}

	public static void unschedule(String className) {
		ProcessToolSchedulerService service = getRegistry().getRegisteredService(ProcessToolSchedulerService.class);

		service.cancelScheduledJobGroup(className);
	}

	private ProcessToolSchedulerServiceUtil() {}
}
