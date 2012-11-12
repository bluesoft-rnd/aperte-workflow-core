package pl.net.bluesoft.rnd.pt.ext.jbpm.query;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;

/** 
 * Bpm query with additional parameters for process instance filters
 * 
 * @author Maciej Pawlak
 *
 */
public class BpmTaskFilterQuery extends BpmTaskQuery
{
	/** Additional condition to main query to add filter for queue type */
	private static final String TASK_NAME_CONDITION = " and task.activity_name_ in (:taskNames) ";
	
	/** Additional condition to main query to add filter for owner login */
	private static final String OWNER_LOGINS_CONDITION = " and queue.user_login IN (:ownerIds) ";
	
	/** Additional condition to main query to add filter for created before filter */
	private static final String CREATED_BEFORE_CONDITION = " and process.createdate <= :createdBeforeDate ";
	
	/** Additional condition to main query to add filter for created after date */
	private static final String CREATED_AFTER_CONDITION = " and process.createdate >= :createdAfterDate ";
	
	/** Additional condition to main query to add filter for given queues */
	private static final String QUEUES_CONDITION = 	" and exists (select participant.task_ from jbpm4_participation participant " +
													"where participant.task_=task.htask_ AND participant.type_ = 'candidate' " +
													"AND participant.groupid_ IN (:queueIds) AND participant.userid_ IS null) ";
	
	public BpmTaskFilterQuery(ProcessToolContext ctx) 
	{
		super(ctx);
	}

	/** Add restriction for task name */
	public void addTaskNamesCondtition(Collection<String> taskNames)
	{
		addCondition(TASK_NAME_CONDITION);
		addParameter("taskNames", taskNames);
	}

	/** Add restriction for owners logins */
	public void addOwnerLoginsCondtition(Collection<String> ownerLogins)
	{
		addCondition(OWNER_LOGINS_CONDITION);
		addParameter("ownerIds", ownerLogins);
	}
	
	/** Add restriction for created before date for process instance */
	public void addCreatedBeforeCondition(Date createdBeforeDate)
	{
		addCondition(CREATED_BEFORE_CONDITION);
		addParameter("createdBeforeDate", createdBeforeDate);
	}
	
	/** Add restriction for created after date for process instance */
	public void addCreatedAfterCondition(Date createdAfterDate)
	{
		addCondition(CREATED_AFTER_CONDITION);
		addParameter("createdAfterDate", createdAfterDate);
	}

	/** Add restriction for queue ids */
	public void addQueuesCondition(Set<String> queues) 
	{
		addCondition(QUEUES_CONDITION);
		addParameter("queueIds", queues);
		
	}


}
