package pl.net.bluesoft.rnd.pt.ext.deadline;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class HandleDeadlineJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();

        String userLogin = (String) dataMap.get("userLogin");
        String processInstanceId = (String) dataMap.get("processInstanceId");
        String taskName = (String) dataMap.get("taskName");
        String templateName = (String) dataMap.get("templateName");
        DeadlineEngine engine = (DeadlineEngine) dataMap.get("deadlineEngine");

        engine.handleDeadlineJob(processInstanceId, taskName, userLogin, templateName);
    }
}
