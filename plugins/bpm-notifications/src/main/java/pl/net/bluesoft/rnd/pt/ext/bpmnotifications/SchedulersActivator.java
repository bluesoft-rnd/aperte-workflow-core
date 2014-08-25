package pl.net.bluesoft.rnd.pt.ext.bpmnotifications;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

import java.util.logging.Logger;

import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.pt.ext.sched.service.ProcessToolSchedulerService;


/**
 *  Activator class for the notifications schedule jobs
 *  
 * @author Maciej Pawlak
 *
 */
public class SchedulersActivator 
{
	private static final Logger logger = Logger.getLogger(SchedulersActivator.class.getName());
	
	private ProcessToolRegistry registry;
	
	public SchedulersActivator(ProcessToolRegistry registry)
	{
		this.registry = registry;
	}
	
	/** Scheduler job for period notifications sending */
    public void scheduleNotificationsSend(BpmNotificationEngine engine) 
    {
        ProcessToolSchedulerService service = getSchedulerService();
        
        JobDataMap dataMap = new JobDataMap();
        dataMap.put("bpmNotificationEngine", engine);

        String identity = "engine_notifications";

        JobDetail jobDetail = JobBuilder.newJob(HandleEmailsJob.class)
                .withIdentity(identity, HandleEmailsJob.class.getName())
                .usingJobData(dataMap)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(identity, HandleEmailsJob.class.getName())
                .withSchedule(simpleSchedule().withIntervalInSeconds(30).repeatForever())
                .forJob(jobDetail)
                .build();

        logger.info("Scheduling notifications job handler");
        
        service.scheduleJob(jobDetail, trigger);
    }

    
    private ProcessToolSchedulerService getSchedulerService() {
        return registry.getRegisteredService(ProcessToolSchedulerService.class);
    }

}
