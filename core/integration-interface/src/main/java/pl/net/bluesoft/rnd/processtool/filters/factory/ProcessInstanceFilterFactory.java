package pl.net.bluesoft.rnd.processtool.filters.factory;

import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceFilter;
import pl.net.bluesoft.rnd.processtool.model.QueueType;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueue;

import static pl.net.bluesoft.rnd.processtool.model.QueueType.*;

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
		return getProcessInstanceFilter(user, "activity.created.tasks", OWN_IN_PROGRESS, OWN_IN_QUEUE);
	}
	
	/** Methods creates new filter which returns tasks created by other users, but assigned to given user */
	public ProcessInstanceFilter createOthersTaskAssignedToMeFilter(UserData user)
	{
		return getProcessInstanceFilter(user, "activity.assigned.tasks", ASSIGNED_TO_CURRENT_USER);
	}
	
	/** Methods creates new filter which returns tasks created by given user and assigned to him */
	public ProcessInstanceFilter createMyTasksAssignedToMeFilter(UserData user)
	{
		return getProcessInstanceFilter(user, "activity.created.assigned.tasks", OWN_ASSIGNED);
	}

	/** Methods creates new filter which returns user closed tasks */
	public ProcessInstanceFilter createMyClosedTasksFilter(UserData user)
	{
		return getProcessInstanceFilter(user, "activity.created.closed.tasks", OWN_FINISHED);
	}
	
	/** Methods creates new filter which returns user closed tasks */
	public ProcessInstanceFilter createQueuedTaskFilter(UserData user, ProcessQueue processQueue)
	{
		return getProcessInstanceFilterForQueue(user, processQueue);
	}

	
	/** Methods creates new filter which returns tasks created by given user, but done by others */
	public ProcessInstanceFilter createSubstitutedTaskDoneByOthersFilter(UserData substitutedUser)
	{
		return getProcessInstanceFilter(substitutedUser, "activity.subst.created.tasks", OWN_IN_PROGRESS, OWN_IN_QUEUE);
	}
	
	/** Methods creates new filter which returns tasks created by other users, but assigned to given user */
	public ProcessInstanceFilter createSubstitutedOthersTaskAssignedToMeFilter(UserData substitutedUser)
	{
		return getProcessInstanceFilter(substitutedUser, "activity.subst.assigned.tasks", ASSIGNED_TO_CURRENT_USER);
	}
	
	/** Methods creates new filter which returns tasks created by given user and assigned to him */
	public ProcessInstanceFilter createSubstitutedTasksAssignedToMeFilter(UserData substitutedUser)
	{
		return getProcessInstanceFilter(substitutedUser, "activity.subst.created.assigned.tasks", OWN_ASSIGNED);
	}

	/** Methods creates new filter which returns user closed tasks */
	public ProcessInstanceFilter createSubstitutedClosedTasksFilter(UserData substitutedUser)
	{
		return getProcessInstanceFilter(substitutedUser, "activity.subst.created.closed.tasks", OWN_FINISHED);
	}
	
	/** Methods creates new filter which returns user closed tasks */
	public ProcessInstanceFilter createOtherUserTaskForSubstitutedUser(UserData substitutedUser)
	{
		return getProcessInstanceFilter(substitutedUser, "activity.other.users.tasks", ASSIGNED_TO_CURRENT_USER);
	}
	
	private ProcessInstanceFilter getProcessInstanceFilterForQueue(UserData user, ProcessQueue processQueue)
	{
		ProcessInstanceFilter instanceFilter = getProcessInstanceFilter(user, "user.queue.name."+processQueue.getName());
		
		instanceFilter.getQueues().add(processQueue.getName());
		
		return instanceFilter;
	}
	
	private ProcessInstanceFilter getProcessInstanceFilter(UserData user, String name, QueueType... types)
	{
		ProcessInstanceFilter pif = new ProcessInstanceFilter();
		pif.setFilterOwner(user);
		pif.setName(name);
		
		for(QueueType queueType: types) {
			pif.addQueueType(queueType);
		}
		return pif;
	}

}
