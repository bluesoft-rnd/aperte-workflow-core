package pl.net.bluesoft.rnd.processtool.filters.factory;

import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceFilter;
import pl.net.bluesoft.rnd.processtool.model.QueueType;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueue;

/**
 * Filter factory to encapsulte filter creation logic
 * @author Maciej Pawlak
 *
 */
public class ProcessInstanceFilterFactory 
{
	public ProcessInstanceFilterFactory() 
	{
	}

	/** Methods creates new filter which returns tasks created by given user, but done by others */
	public ProcessInstanceFilter createMyTaskDoneByOthersFilter(UserData user)
	{
		return getProcessInstanceFilter(user,user,null,"activity.created.tasks", QueueType.OWN_IN_PROGRESS, QueueType.OWN_IN_QUEUE);
	}
	
	/** Methods creates new filter which returns tasks created by other users, but assigned to given user */
	public ProcessInstanceFilter createOthersTaskAssignedToMeFilter(UserData user)
	{
		return getProcessInstanceFilter(user,null,user,"activity.assigned.tasks", QueueType.ASSIGNED_TO_CURRENT_USER);
	}
	
	/** Methods creates new filter which returns tasks created by given user and assigned to him */
	public ProcessInstanceFilter createMyTasksAssignedToMeFilter(UserData user)
	{
		return getProcessInstanceFilter(user,user,user,"activity.created.assigned.tasks", QueueType.ASSIGNED_TO_CURRENT_USER);
	}

	/** Methods creates new filter which returns tasks assigned to given user */
	public ProcessInstanceFilter createTaskAssignedToMeFilter(UserData user)
	{
		return getProcessInstanceFilter(user,null,user,"activity.assigned.tasks", QueueType.ASSIGNED_TO_CURRENT_USER);
	}
	
	/** Methods creates new filter which returns user closed tasks */
	public ProcessInstanceFilter createMyClosedTasksFilter(UserData user)
	{
		return getProcessInstanceFilter(user,user,null,"activity.created.closed.tasks", QueueType.OWN_FINISHED);
	}
	
	/** Methods creates new filter which returns user closed tasks */
	public ProcessInstanceFilter createQueuedTaskFilter(UserData user, ProcessQueue processQueue)
	{
		return getProcessInstanceFilterForQueue(user, processQueue);
	}

	
	/** Methods creates new filter which returns tasks created by given user, but done by others */
	public ProcessInstanceFilter createSubstitutedTaskDoneByOthersFilter(UserData substitutedUser)
	{
		return getProcessInstanceFilter(substitutedUser,substitutedUser,null,"activity.subst.created.tasks", QueueType.OWN_IN_PROGRESS);
	}
	
	/** Methods creates new filter which returns tasks created by other users, but assigned to given user */
	public ProcessInstanceFilter createSubstitutedOthersTaskAssignedToMeFilter(UserData substitutedUser)
	{
		return getProcessInstanceFilter(substitutedUser,null,substitutedUser,"activity.subst.assigned.tasks", QueueType.ASSIGNED_TO_CURRENT_USER);
	}
	
	/** Methods creates new filter which returns tasks created by given user and assigned to him */
	public ProcessInstanceFilter createSubstitutedTasksAssignedToMeFilter(UserData substitutedUser)
	{
		return getProcessInstanceFilter(substitutedUser,substitutedUser,substitutedUser,"activity.subst.created.assigned.tasks", QueueType.ASSIGNED_TO_CURRENT_USER);
	}

	/** Methods creates new filter which returns tasks assigned to given user */
	public ProcessInstanceFilter createTasksAssignedToSubstitutedUserFilter(UserData substitutedUser)
	{
		return getProcessInstanceFilter(substitutedUser,null,substitutedUser,"activity.subst.assigned.tasks", QueueType.ASSIGNED_TO_CURRENT_USER);
	}
	
	/** Methods creates new filter which returns user closed tasks */
	public ProcessInstanceFilter createSubstitutedClosedTasksFilter(UserData substitutedUser)
	{
		return getProcessInstanceFilter(substitutedUser,substitutedUser,null,"activity.subst.created.closed.tasks", QueueType.OWN_FINISHED);
	}
	
	/** Methods creates new filter which returns user closed tasks */
	public ProcessInstanceFilter createOtherUserTaskForSubstitutedUser(UserData substitutedUser)
	{
		return getProcessInstanceFilter(substitutedUser,
				null,
				substitutedUser,
				"activity.other.users.tasks",
				QueueType.ASSIGNED_TO_CURRENT_USER);
	}
	
	private ProcessInstanceFilter getProcessInstanceFilterForQueue(UserData user, ProcessQueue processQueue)
	{
		ProcessInstanceFilter instanceFilter = getProcessInstanceFilter(user,user,null,"user.queue.name."+processQueue.getName(), QueueType.OWN_IN_QUEUE);
		
		instanceFilter.getQueues().add(processQueue.getName());
		
		return instanceFilter;
	}
	
	private ProcessInstanceFilter getProcessInstanceFilter(UserData user, UserData creator, UserData owner, String name, QueueType ... types)
	{
		ProcessInstanceFilter pif = new ProcessInstanceFilter();
		pif.setFilterOwner(user);
		pif.setName(name);
		
		for(QueueType queueType: types)
			pif.addQueueType(queueType);
		
		if(creator != null)
			pif.getCreators().add(creator);

		if(owner != null)
			pif.getOwners().add(owner);

		return pif;
	}

}
