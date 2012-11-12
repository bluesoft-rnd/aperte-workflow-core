package pl.net.bluesoft.rnd.pt.ext.jbpm;

import java.util.Collection;

import org.hibernate.Session;

import pl.net.bluesoft.rnd.processtool.dao.UserProcessQueueDAO;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.QueueType;
import pl.net.bluesoft.rnd.processtool.model.UserProcessQueue;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.MutableBpmTask;
import pl.net.bluesoft.rnd.processtool.userqueues.IUserProcessQueueManager;

/**
 * Manager for the process instance queues
 * 
 * @author Maciej Pawlak
 *
 */
public class UserProcessQueueManager implements IUserProcessQueueManager
{
	Session session;
	UserProcessQueueDAO queueDao;
	
	public UserProcessQueueManager(Session session, UserProcessQueueDAO userProcessQueueDAO)
	{
		this.session = session;
		this.queueDao = userProcessQueueDAO;
	}
	

	public void onTaskAssigne(BpmTask bpmTask)
	{
		processTaskAssigne(bpmTask);
	}
	
	@Override
	public void onQueueAssigne(MutableBpmTask bpmTask) 
	{
		Long processId = bpmTask.getProcessInstance().getId();
		String taskIdString = bpmTask.getInternalTaskId();
		
		/* There is at least one owner - creator by default */
		for(String ownerLogin: bpmTask.getProcessInstance().getOwners())
			updateUserProcessQueue(taskIdString, processId, ownerLogin, QueueType.OWN_IN_QUEUE);
	}
	
	@Override
	public void onTaskFinished(BpmTask bpmTask) 
	{
		String taskIdString = bpmTask.getInternalTaskId();
		Long taskId = Long.parseLong(taskIdString);
		
		Collection<UserProcessQueue> userProcessQueues = queueDao.getAllUserProcessQueueByTaskId(taskId);
		
		/* Delete all process user task with assigned task id. It will delete all user queues
		 * for creator, assigne and owners
		 */
		queueDao.delete(userProcessQueues);
	}
	
	@Override
	public void onProcessFinished(ProcessInstance processInstance, BpmTask bpmTask) 
	{
		/* Get all queue elements for given process id and delete them */
		Long processId = processInstance.getId();
		String creatorLogin = bpmTask.getCreator();
		
		/* Create new queue element that is stored as finished process */
		UserProcessQueue finishedProcess = new UserProcessQueue();
		finishedProcess.setLogin(creatorLogin);
		finishedProcess.setProcessId(processId);
		finishedProcess.setQueueType(QueueType.OWN_FINISHED);
		finishedProcess.setTaskId(Long.parseLong(bpmTask.getInternalTaskId()));
		queueDao.saveOrUpdate(finishedProcess);
	}


	@Override
	public void onProcessHalted(ProcessInstance processInstance, BpmTask task) 
	{
		//deleteProcessAllocations(processInstance);
		/* NOP */
		
	}
	
	private void processTaskAssigne(BpmTask bpmTask)
	{
		String assignee = bpmTask.getAssignee();
		Long processId = bpmTask.getProcessInstance().getId();
		String taskIdString = bpmTask.getInternalTaskId();
		
		/* Is task assigned to one of the owners? */
		boolean taskAssignedToOneOfOwners = false;
		
		/* There is at least one owner - creator by default */
		for(String ownerLogin: bpmTask.getProcessInstance().getOwners())
		{
			/* Assign process to its owner queue */
			if(ownerLogin.equals(assignee))
			{
				updateUserProcessQueue(taskIdString, processId, ownerLogin, QueueType.ASSIGNED_TO_CURRENT_USER);
				taskAssignedToOneOfOwners = true;
			}
			
			/* Assign owner process to someone else. Create queue element to owner "mine assigned to others"
			 * and element to other person queue "others assigned to me" */
			else
			{
				/* If task is already assigned to assigne as it's own task or there is no assigne,
				 * do not change queue to others-assigned */
				boolean shouldAddToOthersAssignedQueue = !taskAssignedToOneOfOwners && assignee != null;
				
				if(shouldAddToOthersAssignedQueue)
					updateUserProcessQueue(taskIdString, processId, assignee, QueueType.ASSIGNED_TO_CURRENT_USER);
				
				updateUserProcessQueue(taskIdString, processId, ownerLogin, QueueType.OWN_IN_PROGRESS); 
			}
		}
	}
	
	private void updateUserProcessQueue(String taskIdString, Long processId, String assigneLogin, QueueType type)
	{
		Long taskId = Long.parseLong(taskIdString);
		UserProcessQueue userProcessQueue = queueDao.getUserProcessQueueByTaskId(taskId, assigneLogin);
		
		/* The queue element for given process exists with type "mine assiegned to me". Change its type and save */
		if(userProcessQueue != null)
		{
			userProcessQueue.setQueueType(type);
		}
		/* Otherwise, create new process queue with correct type */
		else 
		{
			userProcessQueue = new UserProcessQueue();
			userProcessQueue.setLogin(assigneLogin);
			userProcessQueue.setProcessId(processId);
			userProcessQueue.setTaskId(taskId);
			userProcessQueue.setQueueType(type);
		}
		
		queueDao.saveOrUpdate(userProcessQueue);
	}

}
