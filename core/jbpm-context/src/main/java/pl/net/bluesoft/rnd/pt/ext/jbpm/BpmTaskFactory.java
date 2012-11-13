package pl.net.bluesoft.rnd.pt.ext.jbpm;

import org.jbpm.pvm.internal.history.model.HistoryTaskInstanceImpl;
import org.jbpm.pvm.internal.task.TaskImpl;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.MutableBpmTask;

/**
 * 
 * Bpm task factory
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class BpmTaskFactory
{
	ProcessToolContext ctx;
	
	public BpmTaskFactory(ProcessToolContext ctx)
	{
		this.ctx = ctx;
	}
	
   	public BpmTask create(TaskImpl task, ProcessInstance pi) 
   	{
   		MutableBpmTask t = new MutableBpmTask();
   		t.setProcessInstance(pi);
   		t.setAssignee(task.getAssignee());
   		UserData ud = ctx.getUserDataDAO().loadUserByLogin(task.getAssignee());
   		if (ud == null) {
   			ud = new UserData();
   			ud.setLogin(task.getAssignee());
   		}
   		t.setOwner(ud);
   		t.setTaskName(task.getActivityName());
   		t.setInternalTaskId(task.getId());
   		t.setExecutionId(task.getExecutionId());
   		t.setCreateDate(task.getCreateTime());
   		t.setFinishDate(task.getDuedate());
   		t.setFinished(false);
   		
   		return t;
   	}
   	
   	public BpmTask create(HistoryTaskInstanceImpl task, ProcessInstance pi) 
   	{
   		MutableBpmTask t = new MutableBpmTask();
   		t.setProcessInstance(pi);
   		t.setAssignee(task.getHistoryTask().getAssignee());
   		UserData ud = ctx.getUserDataDAO().loadUserByLogin(task.getHistoryTask().getAssignee());
   		if (ud == null) {
   			ud = new UserData();
   			ud.setLogin(task.getHistoryTask().getAssignee());
   		}
   		t.setOwner(ud);
   		t.setTaskName(task.getActivityName());
   		t.setInternalTaskId(task.getHistoryTask().getId());
   		t.setExecutionId(task.getExecutionId());
   		t.setCreateDate(task.getStartTime());
   		t.setFinishDate(task.getEndTime());
   		t.setFinished(false);
   		return t;
   	}
   	
   	
}
