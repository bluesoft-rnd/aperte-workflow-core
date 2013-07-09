package pl.net.bluesoft.rnd.pt.ext.jbpm.service.query;

import org.hibernate.SQLQuery;
import org.hibernate.type.StandardBasicTypes;
import org.jbpm.task.Status;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.QueueType;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.BpmTaskBean;
import pl.net.bluesoft.util.lang.cquery.func.F;

import java.util.*;

import static pl.net.bluesoft.rnd.processtool.ProcessToolContext.Util.getThreadProcessToolContext;
import static pl.net.bluesoft.util.lang.cquery.CQuery.from;

/**
 * Class to build main query to get bpm tasks
 *
 * @author Maciej Pawlak
 */
public class BpmTaskQuery {
	private static class QueryParameter {
		private final String key;
		private final Object value;

		public QueryParameter(String key, Object value) {
			this.key = key;
			this.value = value;
		}

		public String getKey() {
			return key;
		}

		public Object getValue() {
			return value;
		}

		@Override
		public String toString() {
			return "QueryParameter{" + "key='" + key + '\'' + ", value=" + value + '}';
		}
	}

	private enum QueryType {
		COUNT, LIST,
	}

	private String user;
	private Collection<String> owners;
	private Collection<QueueType> virtualQueues;
	private Collection<String> queues;
	private Collection<String> taskNames;
	private Date createdBefore;
	private Date createdAfter;
	private boolean orderByCreateDateDesc;

	private int offset;
	private int limit = -1;

	public BpmTaskQuery user(String user) {
		this.user = user;
		return this;
	}

	public BpmTaskQuery owners(Collection<String> owners) {
		this.owners = owners;
		return this;
	}

	public BpmTaskQuery virtualQueues(Collection<QueueType> virtualQueues) {
		this.virtualQueues = virtualQueues;
		return this;
	}

	public BpmTaskQuery queues(Set<String> queues) {
		this.queues = queues;
		return this;
	}

	public BpmTaskQuery taskNames(Collection<String> taskNames) {
		this.taskNames = taskNames;
		return this;
	}

	public BpmTaskQuery createdBefore(Date createdBefore) {
		this.createdBefore = createdBefore;
		return this;
	}

	public BpmTaskQuery createdAfter(Date createdAfter) {
		this.createdAfter = createdAfter;
		return this;
	}

	public BpmTaskQuery orderByCreateDateDesc() {
		this.orderByCreateDateDesc = true;
		return this;
	}

	public BpmTaskQuery page(int offset, int limit) {
		this.offset = offset;
		this.limit = limit;
		return this;
	}

	public int count() {
		SQLQuery query = getQuery(QueryType.COUNT);
		Number result = (Number)query.uniqueResult();
		return result.intValue();
	}

	public List<BpmTask> list() {
		SQLQuery query = getQuery(QueryType.LIST);
		List<Object[]> queryResults = query.list();

		List<BpmTask> result = new ArrayList<BpmTask>();

		for (Object[] resultRow : queryResults) {
			ProcessInstance processInstance = (ProcessInstance)resultRow[0];
			UserData owner = (UserData)resultRow[1];
			int taskId = (Integer)resultRow[2];
			String assignee = (String)resultRow[3];
			String groupId = (String)resultRow[4];
			String taskName = (String)resultRow[5];
			Date createDate = (Date)resultRow[6];
			Date finishDate = (Date)resultRow[7];
			String status = (String)resultRow[8];

			BpmTaskBean bpmTask = new BpmTaskBean();

			bpmTask.setProcessInstance(processInstance);
			bpmTask.setOwner(owner);
			bpmTask.setExecutionId(processInstance.getInternalId());
			bpmTask.setInternalTaskId(String.valueOf(taskId));
			bpmTask.setAssignee(assignee);
			bpmTask.setGroupId(groupId);
			bpmTask.setTaskName(taskName);
			bpmTask.setCreateDate(createDate);
			bpmTask.setFinishDate(finishDate);
			bpmTask.setFinished(Status.valueOf(status) == Status.Completed);

			result.add(bpmTask);
		}
		return result;
	}

	private SQLQuery getQuery(QueryType queryType) {
		List<QueryParameter> queryParameters = new ArrayList<QueryParameter>();
		String queryString = getQueryString(queryType, queryParameters);
		SQLQuery query = getThreadProcessToolContext().getHibernateSession().createSQLQuery(queryString);

		if (queryType == QueryType.LIST) {
			query.addEntity("process", ProcessInstance.class)
					.addEntity("owner", UserData.class)
					.addScalar("taskId", StandardBasicTypes.INTEGER)
					.addScalar("assignee", StandardBasicTypes.STRING)
					.addScalar("groupId", StandardBasicTypes.STRING)
					.addScalar("taskName", StandardBasicTypes.STRING)
					.addScalar("createdOn", StandardBasicTypes.DATE)
					.addScalar("completedOn", StandardBasicTypes.DATE)
					.addScalar("taskStatus", StandardBasicTypes.STRING);
		}

		for (QueryParameter parameter : queryParameters) {
			if (parameter.getValue() instanceof Collection<?>) {
				query.setParameterList(parameter.getKey(), (Collection<?>)parameter.getValue());
			}
			else {
				query.setParameter(parameter.getKey(), parameter.getValue());
			}
		}

		if (limit > 0) {
			query.setMaxResults(limit);
		}

		query.setFirstResult(offset);

		return query;
	}

	private String getQueryString(QueryType queryType, List<QueryParameter> queryParameters) {
		StringBuilder sb = new StringBuilder("SELECT ");

		if (queryType == QueryType.COUNT) {
			sb.append("COUNT(*)");
		}
		else {
			sb.append("process.*, owner.*, task_.id as taskId, task_.actualowner_id as assignee, ");
			sb.append("CASE WHEN task_.actualowner_id IS NULL THEN potowners.entity_id END as groupId, ");
			sb.append("i18ntext_.shortText as taskName, task_.createdOn as createdOn, task_.completedOn as completedOn, ");
			sb.append("task_.status as taskStatus");
		}

		sb.append(" FROM pt_process_instance process JOIN task task_ ON CAST(task_.processinstanceid AS VARCHAR(10)) = process.internalId");

		if (queues != null || queryType == QueryType.LIST) {
			sb.append(" JOIN peopleassignments_potowners potowners ON potowners.task_id = task_.id");
		}

		if (taskNames != null || queryType == QueryType.LIST) {
			sb.append(" JOIN i18ntext i18ntext_ ON i18ntext_.task_names_id = task_.id");
		}

		if (virtualQueues != null) {
			sb.append(" JOIN pt_user_data creator ON creator.id = process.creator_id");
		}

		if (queryType == QueryType.LIST) {
			sb.append(" LEFT JOIN pt_user_data owner ON owner.login = task_.actualowner_id");
		}

		sb.append(" WHERE 1=1");

		if (owners != null) {
			sb.append(" AND EXISTS(SELECT * FROM pt_process_instance_owners powner WHERE powner.process_id = process.id AND owners IN (:owners))");
			queryParameters.add(new QueryParameter("owners", owners));
		}

		if (virtualQueues != null && user != null) {
			sb.append(from(virtualQueues).select(GET_VIRTUAL_QUEUES).toString(" OR ", " AND (", ")"));
			queryParameters.add(new QueryParameter("user", user));
		}

		if (queues != null) {
			sb.append(" AND task_.actualowner_id IS NULL AND potowners.entity_id IN (:queues)");
			queryParameters.add(new QueryParameter("queues", queues));
		}

		if (taskNames != null) {
			sb.append(" AND i18ntext_.shortText IN (:taskNames)");
			queryParameters.add(new QueryParameter("taskNames", taskNames));
		}

		if (createdBefore != null) {
			sb.append(" AND task_.createdOn <= :createdBefore");
			queryParameters.add(new QueryParameter("createdBefore", createdBefore));
		}

		if (createdAfter != null) {
			sb.append(" AND task_.createdOn >= :createdAfter");
			queryParameters.add(new QueryParameter("createdAfter", createdAfter));
		}

		if (queryType == QueryType.LIST && orderByCreateDateDesc) {
			sb.append(" ORDER BY task_.createdOn DESC");
		}

		return sb.toString();
	}

	private static final F<QueueType,Object> GET_VIRTUAL_QUEUES = new F<QueueType, Object>() {
		@Override
		public Object invoke(QueueType virtualQueue) {
			return getVirtualQueueCondition(virtualQueue);
		}
	};

	private static String getVirtualQueueCondition(QueueType virtualQueue) {
		switch (virtualQueue) {
			case OWN_IN_PROGRESS:
				return "(creator.login = :user AND task_.actualowner_id != :user AND task_.status NOT IN ('Completed'))";
			case OWN_ASSIGNED:
				return "(creator.login = :user AND task_.actualowner_id = :user AND task_.status NOT IN ('Completed'))";
			case OWN_IN_QUEUE:
				return "(creator.login = :user AND task_.actualowner_id IS NULL AND task_.status NOT IN ('Completed'))";
			case OWN_FINISHED:
				return "(creator.login = :user AND task_.actualowner_id = :user AND task_.status IN ('Completed'))";
			case ASSIGNED_TO_CURRENT_USER:
				return "(creator.login != :user AND task_.actualowner_id = :user AND task_.status NOT IN ('Completed'))";
			default:
				throw new RuntimeException("Unhandled type: " + virtualQueue);
		}
	}

	@Override
	public String toString() {
		return "BpmTaskQuery{" +
				"user='" + user + '\'' +
				", owners=" + owners +
				", virtualQueues=" + virtualQueues +
				", queues=" + queues +
				", taskNames=" + taskNames +
				", createdBefore=" + createdBefore +
				", createdAfter=" + createdAfter +
				", orderByCreateDateDesc=" + orderByCreateDateDesc +
				", offset=" + offset +
				", limit=" + limit +
				'}';
	}
}
