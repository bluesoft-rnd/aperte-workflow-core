package pl.net.bluesoft.rnd.pt.ext.jbpm.query;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.SQLQuery;
import org.jbpm.pvm.internal.history.model.HistoryTaskInstanceImpl;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.QueueType;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.MutableBpmTask;
import pl.net.bluesoft.rnd.pt.ext.jbpm.BpmTaskFactory;

/**
 * Class to build main query to get bpm tasks 
 * 
 * @author Maciej Pawlak
 *
 */
public class BpmTaskQuery 
{
	/** Normal query select to retrive entities */
	private static final String LIST_QUERY = "select DISTINCT task.*, process.* ";
	
	/** Count query select to load only number of result to memory */
	private static final String COUNT_QUERY = "select count(*) ";
	
	/** Main query to get task with correlated processes from user process queue */
	public static final String GET_BPM_TASKS_QUERY = 
			"from pt_user_process_queue queue, jbpm4_hist_actinst task, pt_process_instance process " +
			"where queue.task_id = task.htask_ and process.id = queue.process_id ";
	
	/** Additional condition to main query to add filter for user login to who task and process are assigned */
	private static final String USER_LOGIN_CONDITION = " and queue.user_login = :userLogin ";
	
	/** Additional condition to main query to add filter for queue type */
	private static final String QUEUE_TYPE_CONDITION = " and queue.queue_type in (:queueTypes) ";
	
	/** Resuls sort order */
	private static final String SORY_BY_DATE_ORDER = " order by task.start_ desc";
	
	/** String builder to build query */
	private StringBuilder queryBuilder;
	
	private Collection<QueryParameter> queryParameters;
	
	/** Current session */
	private ProcessToolContext ctx;
	
	/** Limit for results rows */
	private int maxResultsLimit;
	
	/** Offset for results rows, used for paged views */
	private int resultsOffset;
	
	public BpmTaskQuery(ProcessToolContext ctx)
	{
		this.ctx = ctx;
		
		queryBuilder = new StringBuilder(GET_BPM_TASKS_QUERY);
		queryParameters = new ArrayList<BpmTaskQuery.QueryParameter>();
		
		this.maxResultsLimit = 0;
		this.resultsOffset = 0;
	}
	
	/** Add restriction for user login to who process and task are assigned */
	public void addUserLoginCondition(String userLogin)
	{
		addCondition(USER_LOGIN_CONDITION);
		addParameter("userLogin", userLogin);
	}
	
	/** Add restriction for user login to who process and task are assigned */
	public void addQueueTypeCondition(Collection<QueueType> queueTypes)
	{
		addCondition(QUEUE_TYPE_CONDITION);
		
		/* Map all enumerations to string, for valid type cast in database */
		Collection<String> queueTypesString = new ArrayList<String>();
		for(QueueType queueType: queueTypes)
			queueTypesString.add(queueType.toString());
		
		addParameter("queueTypes", queueTypesString);
	}
	
	/** Get results count. No entities are loaded to memory using this */
	public int getBpmTaskCount()
	{
		/* Build query */
		SQLQuery query = getCountQuery();
		
		Number resultsCount = (Number)query.uniqueResult();
		
		return resultsCount.intValue();
	}
	
	/** Get bpm tasks from initialized query */
	@SuppressWarnings("unchecked")
	public List<BpmTask> getBpmTasks()
	{	
		/* Build query */
		SQLQuery query = getQuery();
		
		/* Get query results */
		List<Object[]> queueResults = query.list();
		
		List<BpmTask> result = new ArrayList<BpmTask>();
		
		BpmTaskFactory taskFactory = new BpmTaskFactory(ctx);
		
   		
		/* Every row is one queue element with jbpm task as first column and process instance as second */
   		for(Object[] resultRow: queueResults)
   		{
   			
   			HistoryTaskInstanceImpl taskInstance = (HistoryTaskInstanceImpl)resultRow[0];
   			ProcessInstance processInstance = (ProcessInstance)resultRow[1];
   			
   			/* Map process and jbpm task to system's bpm task */
   			BpmTask task = taskFactory.create(taskInstance, processInstance);
   			
   			result.add(task);
   		}
   		
   		return result;
	}
	
	protected void addCondition(String conditionString)
	{
		queryBuilder.append(conditionString);
	}
	
	protected void addParameter(String key, Object value)
	{
		queryParameters.add(new QueryParameter(key, value));
	}
	
	private SQLQuery getCountQuery()
	{
   		SQLQuery query = ctx.getHibernateSession().createSQLQuery(COUNT_QUERY + queryBuilder.toString());
   		
   		/* Add all parameters */
   		for(QueryParameter parameter: queryParameters)
   		{
   			if(parameter.getValue() instanceof Collection<?>)
   				query.setParameterList(parameter.getKey(), (Collection<?>)parameter.getValue());
   			else
   				query.setParameter(parameter.getKey(), parameter.getValue());
   		}
   		
   		return query;
	}
	
	/** Build main query and add stored parameters to it */
	private SQLQuery getQuery()
	{
		/* Add results sort order */
		queryBuilder.append(SORY_BY_DATE_ORDER);
		
   		SQLQuery query = ctx.getHibernateSession().createSQLQuery(LIST_QUERY + queryBuilder.toString())
   				.addEntity("task", HistoryTaskInstanceImpl.class)
   				.addEntity("process", ProcessInstance.class);
   		
   		/* Add all parameters */
   		for(QueryParameter parameter: queryParameters)
   		{
   			if(parameter.getValue() instanceof Collection<?>)
   				query.setParameterList(parameter.getKey(), (Collection<?>)parameter.getValue());
   			else
   				query.setParameter(parameter.getKey(), parameter.getValue());
   		}
   		
   		/* Add limit for max rows count */
   		if(getMaxResultsLimit() > 0)
   			query.setMaxResults(getMaxResultsLimit());
   		
   		query.setFirstResult(resultsOffset);
   		
   		return query;
	}
	
	
   	public int getMaxResultsLimit() {
		return maxResultsLimit;
	}

   	/** Sets max results limit. If equals zero, there is no limit set */
	public void setMaxResultsLimit(int maxResultsLimit) {
		this.maxResultsLimit = maxResultsLimit;
	}

	public int getResultsOffset() {
		return resultsOffset;
	}

	public void setResultsOffset(int resultsOffset) 
	{
		this.resultsOffset = resultsOffset;
	}

	/** Class which provied key-object parameter for query */
	private class QueryParameter
	{
		private String key;
		private Object value;
		
		public QueryParameter(String key, Object value)
		{
			this.key = key;
			this.value = value;
		}
		
		public String getKey() {
			return key;
		}
		public Object getValue() {
			return value;
		}
	}

}
