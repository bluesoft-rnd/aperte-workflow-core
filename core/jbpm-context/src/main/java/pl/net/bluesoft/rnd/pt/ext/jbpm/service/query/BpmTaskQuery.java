package pl.net.bluesoft.rnd.pt.ext.jbpm.service.query;

import org.hibernate.SQLQuery;
import org.hibernate.type.StandardBasicTypes;
import org.jbpm.task.Status;
import pl.net.bluesoft.rnd.processtool.dao.ProcessDefinitionDAO;
import pl.net.bluesoft.rnd.processtool.model.*;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.BpmTaskBean;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.i18n.I18NSourceFactory;
import pl.net.bluesoft.util.lang.cquery.func.F;

import java.util.*;

import static pl.net.bluesoft.rnd.processtool.ProcessToolContext.Util.getThreadProcessToolContext;
import static pl.net.bluesoft.util.lang.Strings.hasText;
import static pl.net.bluesoft.util.lang.cquery.CQuery.from;

/**
 * Class to build main query to get bpm tasks
 *
 * @author Maciej Pawlak
 */
public class BpmTaskQuery {
	private static final String DEADLINE_SUBQUERY =
			"(SELECT MIN(dueDate) FROM pt_ext_pi_deadline_attr da JOIN pt_process_instance_attr pa ON pa.id = da.id " +
			"WHERE pa.process_instance_id = process.id AND da.taskname = i18ntext_.shortText) ";

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
	private String processBpmKey;
	private String searchExpression;
	private Locale locale;
	private QueueOrderCondition sortField;
	private QueueOrder sortOrder;

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

	public void processBpmKey(String processBpmKey) {
		this.processBpmKey = processBpmKey;
	}

	public BpmTaskQuery searchExpression(String searchExpression, Locale locale) {
		this.searchExpression = searchExpression;
		this.locale = locale;
		return this;
	}

	public BpmTaskQuery orderBy(QueueOrderCondition sortField, QueueOrder sortOrder) {
		this.sortField = sortField;
		this.sortOrder = sortOrder;
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

		ProcessDefinitionDAO processDefinitionDAO = getThreadProcessToolContext().getProcessDefinitionDAO();

		for (Object[] resultRow : queryResults) {
			ProcessInstance processInstance = (ProcessInstance)resultRow[0];
			int taskId = (Integer)resultRow[1];
			String assignee = (String)resultRow[2];
			String groupId = (String)resultRow[3];
			String taskName = (String)resultRow[4];
			Date createDate = (Date)resultRow[5];
			Date finishDate = (Date)resultRow[6];
			String status = (String)resultRow[7];
			Long definitionId = (Long)resultRow[8];
			Date taskDeadline = (Date)resultRow[9];

			BpmTaskBean bpmTask = new BpmTaskBean();

			bpmTask.setProcessInstance(processInstance);
			bpmTask.setExecutionId(processInstance.getInternalId());
			bpmTask.setInternalTaskId(String.valueOf(taskId));
			bpmTask.setAssignee(assignee);
			bpmTask.setGroupId(groupId);
			bpmTask.setTaskName(taskName);
			bpmTask.setCreateDate(createDate);
			bpmTask.setFinishDate(finishDate);
			bpmTask.setFinished(Status.valueOf(status) == Status.Completed);
			bpmTask.setProcessDefinition(processDefinitionDAO.getCachedDefinitionById(definitionId));
			bpmTask.setDeadlineDate(taskDeadline);
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
					.addScalar("taskId", StandardBasicTypes.INTEGER)
					.addScalar("assignee", StandardBasicTypes.STRING)
					.addScalar("groupId", StandardBasicTypes.STRING)
					.addScalar("taskName", StandardBasicTypes.STRING)
					.addScalar("createdOn", StandardBasicTypes.DATE)
					.addScalar("completedOn", StandardBasicTypes.DATE)
					.addScalar("taskStatus", StandardBasicTypes.STRING)
					.addScalar("definitionId", StandardBasicTypes.LONG)
					.addScalar("taskDeadline", StandardBasicTypes.DATE);
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
			sb.append("process.*, task_.id as taskId, task_.actualowner_id as assignee, ");
			sb.append("CASE WHEN task_.actualowner_id IS NULL THEN potowners.entity_id END as groupId, ");
			sb.append("i18ntext_.shortText as taskName, task_.createdOn as createdOn, task_.completedOn as completedOn, ");
			sb.append("task_.status as taskStatus, process.definition_id as definitionId, ");
			sb.append(DEADLINE_SUBQUERY);
			sb.append("AS taskDeadline");
		}

		sb.append(" FROM pt_process_instance process JOIN task task_ ON CAST(task_.processinstanceid AS VARCHAR(10)) = process.internalId");

		if (queues != null || queryType == QueryType.LIST) {
			sb.append(" JOIN peopleassignments_potowners potowners ON potowners.task_id = task_.id");
		}

		if (taskNames != null || queryType == QueryType.LIST || hasText(searchExpression)) {
			sb.append(" JOIN i18ntext i18ntext_ ON i18ntext_.task_names_id = task_.id");
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

		if (hasText(processBpmKey)) {
			sb.append(" AND process.definitionname = :processBpmKey");
			queryParameters.add(new QueryParameter("processBpmKey", processBpmKey));
		}

		if (hasText(searchExpression)) {
			sb.append(" AND (");
			sb.append("task_.actualowner_id LIKE '%' || :expression || '%'");
			sb.append(" OR process.creatorLogin LIKE '%' || :expression || '%'");
			sb.append(" OR (CASE WHEN process.externalKey IS NOT NULL THEN process.externalKey ELSE process.internalId END) LIKE '%' || :expression || '%'");
			sb.append(" OR to_char(task_.createdOn, 'YYYY-MM-DD HH24:MI:SS') LIKE :expression || '%'");
			sb.append(" OR EXISTS(SELECT * FROM pt_process_instance_s_attr sattr JOIN pt_process_instance_attr attr ON attr.id = sattr.id");
			sb.append("	WHERE attr.process_instance_id = process.id AND sattr.value_ LIKE '%' || :expression || '%')");
			sb.append(" OR to_char(");
			sb.append(DEADLINE_SUBQUERY);
			sb.append(", 'YYYY-MM-DD HH24:MI:SS') LIKE :expression || '%'");

			queryParameters.add(new QueryParameter("expression", searchExpression.trim()));

			List<Long> definitionDescrKeys = getSearchKeywordMatchingIds(getThreadProcessToolContext()
					.getProcessDefinitionDAO().getProcessDefinitionDescriptions());

			if (!definitionDescrKeys.isEmpty()) {
				sb.append(" OR process.definition_id IN (:searchProcessDefIds)");

				queryParameters.add(new QueryParameter("searchProcessDefIds", definitionDescrKeys));
			}

			List<Long> stateDescrKeys = getSearchKeywordMatchingIds(getThreadProcessToolContext()
					.getProcessDefinitionDAO().getProcessStateDescriptions());

			if (!stateDescrKeys.isEmpty()) {
				sb.append(" OR EXISTS(SELECT * FROM pt_process_state_config psc WHERE psc.id IN (:searchProcessStateIds)");
				sb.append(" AND psc.definition_id = process.definition_id AND psc.name = i18ntext_.shortText)");

				queryParameters.add(new QueryParameter("searchProcessStateIds", stateDescrKeys));
			}

			sb.append(')');
		}

		if (queryType == QueryType.LIST) {
			sb.append(" ORDER BY ").append(getOrder());
		}

		return sb.toString();
	}

	private String getOrder() {
		if (sortField != null) {
			return getOrderField() + ' ' + getOrderDirection();
		}
		return "task_.createdOn DESC";
	}

	private String getOrderField() {
		switch (sortField) {
			case SORT_BY_DATE_ORDER:
				return "task_.createdOn";
			case SORT_BY_CREATE_DATE_ORDER:
				return "process.createdate";
			case SORT_BY_PROCESS_CODE_ORDER:
				return "process.internalid";
			case SORT_BY_PROCESS_STEP_ORDER:
				return "i18ntext_.shortText";
			case SORT_BY_PROCESS_NAME_ORDER:
				return "process.definitionname";
			case SORT_BY_ASSIGNEE_ORDER:
				return "task_.actualowner_id";
			case SORT_BY_CREATOR_ORDER:
				return "process.creatorLogin";
			default:
				throw new RuntimeException("Unhandled order by field " + sortField);
		}
	}

	private String getOrderDirection() {
		return sortOrder == QueueOrder.DESC ? " DESC" : " ASC";
	}

	private static final F<QueueType,Object> GET_VIRTUAL_QUEUES = new F<QueueType, Object>() {
		@Override
		public Object invoke(QueueType virtualQueue) {
			return getVirtualQueueCondition(virtualQueue);
		}
	};

	private static String getVirtualQueueCondition(QueueType virtualQueue) {
		switch (virtualQueue) {
			case MY_TASKS:
				return "(task_.actualowner_id = :user AND task_.status NOT IN ('Completed'))";
			case OWN_IN_PROGRESS:
				return "(process.creatorLogin = :user AND task_.status NOT IN ('Completed'))";
			case OWN_FINISHED:
				return "(process.creatorLogin = :user AND task_.actualowner_id = :user AND task_.status IN ('Completed'))";
			default:
				throw new RuntimeException("Unhandled type: " + virtualQueue);
		}
	}

	private List<Long> getSearchKeywordMatchingIds(Map<Long, String> i18NKeys) {
		if (locale == null) {
			return Collections.emptyList();
		}

		I18NSource i18NSource = I18NSourceFactory.createI18NSource(locale);
		String searchExpressionLC = searchExpression.toLowerCase(locale);

		List<Long> result = new ArrayList<Long>();

		for (Map.Entry<Long, String> entry : i18NKeys.entrySet()) {
			String localizedMessage = i18NSource.getMessage(entry.getValue());

			if (localizedMessage.toLowerCase(locale).contains(searchExpressionLC)) {
				result.add(entry.getKey());
			}
		}
		return result;
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
				", searchExpression='" + searchExpression + '\'' +
				", sortField=" + sortField +
				", sortOrder=" + sortOrder +
				", offset=" + offset +
				", limit=" + limit +
				'}';
	}
}
