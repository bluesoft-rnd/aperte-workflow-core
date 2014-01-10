package pl.net.bluesoft.rnd.processtool.model;

/**
 * This enumeration represents diffrent queue types
 * 
 * @author Maciej Pawlak
 *
 */
public enum QueueType
{
    /** Tasks assigned to users and queues */
    ALL_TASKS("activity.created.all.tasks"),
	/** Tasks assigned to user */
	MY_TASKS("activity.created.assigned.tasks"),
	/** User created task, done by others */
	OWN_IN_PROGRESS("activity.created.tasks"),
	/** User created task in finished state */
	OWN_FINISHED("activity.created.closed.tasks");
    
    private final String queueId;
    
    QueueType(String queueId)
    {
    	this.queueId = queueId;
    }

    public static QueueType fromQueueId(String queueId) 
    {
        for (QueueType ps : values()) 
            if (ps.queueId.equals(queueId))
                return ps;
    
    	return null;
    }
    
    public String getQueueId()
    {
    	return queueId;
    }
}
