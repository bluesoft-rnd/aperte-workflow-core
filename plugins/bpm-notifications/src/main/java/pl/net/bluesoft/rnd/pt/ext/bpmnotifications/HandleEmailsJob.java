package pl.net.bluesoft.rnd.pt.ext.bpmnotifications;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessDeadline;

/**
 * Scheduler witch gathers notfications {@link BpmNotification} from the table, and 
 * sends them in background. This scheduler is result of the long delay which occurs
 * during e-mail sending. Making it in the same thread as gui is inefficient. 
 * 
 * @author Maciej Pawlak
 *
 */
public class HandleEmailsJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException 
    {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();

        BpmNotificationEngine notificationEngine = (BpmNotificationEngine) dataMap.get("bpmNotificationEngine");
        
        notificationEngine.handleNotifications();
    }
}
