package pl.net.bluesoft.rnd.processtool.dao;

import java.util.Collection;

import pl.net.bluesoft.rnd.processtool.hibernate.HibernateBean;
import pl.net.bluesoft.rnd.processtool.model.QueueType;
import pl.net.bluesoft.rnd.processtool.model.UserProcessQueue;

/**
 * DAO for user process queue operations
 * 
 * @author Maciej Pawlak
 *
 */
public interface UserProcessQueueDAO extends HibernateBean<UserProcessQueue> 
{
	/** Get all users process queue elements by given process id and given queue types */
	Collection<UserProcessQueue> getAllUserProcessQueueElements(Long processId, QueueType ... types);
	
    /** Methods returns the user process queue element with given id, created by user with given login and
     * with type = OTHERS_ASSIGNED
     */
	UserProcessQueue getUserProcessAssignedToOthers(Long processId, String creatorLogin);

    /** Methods returns the user process queue element with given id, created by user with given login and
     * with type = OWN_ASSIGNED
     */
	UserProcessQueue getUserProcessAssignedToHim(Long processId, String creatorLogin);

	/** Get the user queue elements contains process allocation from others */
	UserProcessQueue getUserProcessAssignedFromOthers(Long processId, String assigne);

	/** Get user queue element by given taks id 
	 * @param assigneLogin */
	UserProcessQueue getUserProcessQueueByTaskId(Long taskId, String assigneLogin);
	
	/** Get all user process queues by task id */
	Collection<UserProcessQueue> getAllUserProcessQueueByTaskId(Long taskId);

	/** Get the queue length for given user and selected type */
	int getQueueLength(String userLogin, QueueType ... queueTypes);
	
	/** Get the queue length for given user and selected type */
	int getQueueLength(String userLogin, Collection<QueueType> queueTypes);
}
