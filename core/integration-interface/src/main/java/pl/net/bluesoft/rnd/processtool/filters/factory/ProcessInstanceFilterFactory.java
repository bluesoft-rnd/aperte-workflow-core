package pl.net.bluesoft.rnd.processtool.filters.factory;

import pl.net.bluesoft.rnd.processtool.web.view.ProcessInstanceFilter;
import pl.net.bluesoft.rnd.processtool.model.QueueType;

import static pl.net.bluesoft.rnd.processtool.model.QueueType.*;

/**
 * Filter factory to encapsulte filter creation logic
 * @author Maciej Pawlak
 *
 */
public class ProcessInstanceFilterFactory 
{
    public ProcessInstanceFilter createAllTasksFilter(String userLogin) {
        return getProcessInstanceFilter(userLogin, "activity.created.all.tasks", ALL_TASKS);
    }

	public ProcessInstanceFilter createMyTasksFilter(String userLogin) {
		return getProcessInstanceFilter(userLogin, "activity.created.assigned.tasks", MY_TASKS);
	}

	public ProcessInstanceFilter createMyTasksInProgress(String userLogin) {
		return getProcessInstanceFilter(userLogin, "activity.created.tasks", OWN_IN_PROGRESS);
	}

	/** Methods creates new filter which returns user closed tasks */
	public ProcessInstanceFilter createMyClosedTasksFilter(String userLogin)
	{
		return getProcessInstanceFilter(userLogin, "activity.created.closed.tasks", OWN_FINISHED);
	}

	/** Methods creates new filter which returns tasks created by given user and assigned to him */
	public ProcessInstanceFilter createSubstitutedMyTasksFilter(String substitutedUserLogin)
	{
		return getProcessInstanceFilter(substitutedUserLogin, "activity.subst.created.assigned.tasks", MY_TASKS);
	}

	/** Methods creates new filter which returns tasks created by given user, but done by others */
	public ProcessInstanceFilter createSubstitutedTasksInProgress(String substitutedUserLogin)
	{
		return getProcessInstanceFilter(substitutedUserLogin, "activity.subst.created.tasks", OWN_IN_PROGRESS);
	}

	/** Methods creates new filter which returns user closed tasks */
	public ProcessInstanceFilter createSubstitutedClosedTasksFilter(String substitutedUserLogin)
	{
		return getProcessInstanceFilter(substitutedUserLogin, "activity.subst.created.closed.tasks", OWN_FINISHED);
	}

    /** Methods creates new filter which returns user closed tasks */
    public ProcessInstanceFilter createAllTaskForSubstitutedUser(String substitutedUserLogin)
    {
        return getProcessInstanceFilter(substitutedUserLogin, "activity.subst.all.tasks", ALL_TASKS);
    }

	/** Methods creates new filter which returns user closed tasks */
	public ProcessInstanceFilter createOtherUserTaskForSubstitutedUser(String substitutedUserLogin)
	{
		return getProcessInstanceFilter(substitutedUserLogin, "activity.other.users.tasks", MY_TASKS);
	}
	
	private ProcessInstanceFilter getProcessInstanceFilter(String userLogin, String name, QueueType... types)
	{
		ProcessInstanceFilter pif = new ProcessInstanceFilter();
		pif.setFilterOwnerLogin(userLogin);
		pif.setName(name);
		
		for(QueueType queueType: types) {
			pif.addQueueType(queueType);
		}
		return pif;
	}
}
