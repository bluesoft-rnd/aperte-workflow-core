package pl.net.bluesoft.rnd.pt.ext.deadline;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessDeadline;

public class HandleDeadlineJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();

        String processInstanceId = (String) dataMap.get("processInstanceId");
        ProcessDeadline pd = (ProcessDeadline) dataMap.get("deadlineAttribute");
        DeadlineEngine engine = (DeadlineEngine) dataMap.get("deadlineEngine");

        engine.handleDeadlineJob(processInstanceId, pd);
    }
}
