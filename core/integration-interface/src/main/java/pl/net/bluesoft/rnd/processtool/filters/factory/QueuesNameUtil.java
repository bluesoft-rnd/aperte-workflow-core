package pl.net.bluesoft.rnd.processtool.filters.factory;

import org.apache.commons.lang3.StringUtils;


/**
 * Util witch provides tools to support javascript queues refresher
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class QueuesNameUtil 
{
	
	public static String getQueueTaskId(String taskName)
	{
		/* remove whitespaces */
		String fixedTaskName = StringUtils.trimToEmpty(taskName).replace(".", "-").replace(" ", "-");
		
		return "user-task-name-"+fixedTaskName;
	}

	public static String getQueueProcessQueueId(String queueId)
	{
		/* remove whitespaces */
		String fixedQueueId= StringUtils.trimToEmpty(queueId).replace(".", "-").replace(" ", "-");
		
		return "user-queue-name-"+fixedQueueId;
	}

	public static String getSubstitutedQueueTaskId(String taskName, String userLogin)
	{
		/* remove whitespaces */
		String fixedTaskName = StringUtils.trimToEmpty(taskName).replace(".", "-").replace(" ", "-");
		
		return "substituted-"+userLogin+"-user-task-name-"+fixedTaskName;
	}
	
	public static String getSubstitutedRootNodeId(String userLogin)
	{	
		return "substituted-"+userLogin+"-user-root-node";
	}

	public static String getSubstitutedQueueProcessQueueId(String queueId, String userLogin)
	{
		/* remove whitespaces */
		String fixedQueueId= StringUtils.trimToEmpty(queueId).replace(".", "-").replace(" ", "-");
		
		return "substituted-"+userLogin+"-user-queue-name-"+fixedQueueId;
	}
}
