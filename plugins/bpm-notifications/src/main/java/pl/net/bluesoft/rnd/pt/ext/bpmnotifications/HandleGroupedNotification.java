package pl.net.bluesoft.rnd.pt.ext.bpmnotifications;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class HandleGroupedNotification implements Job {
	
	@Override
    public void execute(JobExecutionContext context) throws JobExecutionException 
    {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();

        GroupedNotification groupedNotification = (GroupedNotification) dataMap.get("groupedNotification");
        
        groupedNotification.handleNotifications();
    }
}
