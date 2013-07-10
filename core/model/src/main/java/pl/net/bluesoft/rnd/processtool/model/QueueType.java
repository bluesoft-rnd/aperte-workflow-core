package pl.net.bluesoft.rnd.processtool.model;

import pl.net.bluesoft.util.lang.Strings;

/**
 * This enumeration represents diffrent queue types
 * 
 * @author Maciej Pawlak
 *
 */
public enum QueueType {
	/** User created task, done by others */
    OWN_IN_PROGRESS("activity.created.tasks"), 
    /** User created task, assigned to him */
    OWN_ASSIGNED("activity.created.assigned.tasks"), 
    /** User created task, but it is put in queue */
    OWN_IN_QUEUE("queues"),
    /** User created task in finished state */
    OWN_FINISHED("activity.created.closed.tasks"), 
    /** Others task, assigned to current user */
    ASSIGNED_TO_CURRENT_USER("activity.assigned.tasks");
    
    private String queueId;
    
    private QueueType(String queueId)
    {
    	this.queueId = queueId;
    }

    public static QueueType fromQueueId(String queueId) 
    {
        for (QueueType ps : values()) 
            if (ps.getQueueId().equals(queueId))
                return ps;
    
    	return null;
    }
    
    public String getQueueId()
    {
    	return queueId;
    }
}
