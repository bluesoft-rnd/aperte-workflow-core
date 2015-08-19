package pl.net.bluesoft.rnd.pt.ext.jbpm.service.query;

import org.hibernate.SQLQuery;
import org.hibernate.dialect.Dialect;
import org.hibernate.type.StandardBasicTypes;
import org.jbpm.task.Status;
import pl.net.bluesoft.rnd.processtool.dao.ProcessDefinitionDAO;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.QueueOrder;
import pl.net.bluesoft.rnd.processtool.model.QueueType;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.BpmTaskBean;
import pl.net.bluesoft.rnd.processtool.web.view.IBpmTaskQueryCondition;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.i18n.I18NSourceFactory;
import pl.net.bluesoft.util.lang.cquery.func.F;

import java.sql.Types;
import java.util.*;
import java.util.logging.Logger;

import static pl.net.bluesoft.rnd.processtool.ProcessToolContext.Util.getThreadProcessToolContext;
import static pl.net.bluesoft.util.lang.Strings.hasText;
import static pl.net.bluesoft.util.lang.cquery.CQuery.from;

/**
 * Class to build main query to get bpm tasks
 *
 * @author Maciej Pawlak
 */
public class BpmTaskQuery {

    protected Logger log = Logger.getLogger(BpmTaskQuery.class.getName());


    private static final String DEADLINE_SUBQUERY =
            "(SELECT MIN(dueDate) FROM pt_process_deadline da WHERE da.process_instance_id = process.id AND da.taskname = i18ntext_.shortText) ";

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

    private Dialect hibernateDialect;
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
    private Collection<String> excludedDefinitionIds;
    private List<SortingOrder> columnSorting = new LinkedList<SortingOrder>();

    private class SortingOrder implements Comparable<SortingOrder>
    {
        private String columnName;
        private Integer priority;
        private QueueOrder order;

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public Integer getPriority() {
            return priority;
        }

        public void setPriority(Integer priority) {
            this.priority = priority;
        }

        public QueueOrder getOrder() {
            return order;
        }

        public void setOrder(QueueOrder order) {
            this.order = order;
        }

        @Override
        public int compareTo(SortingOrder o) {
            return this.priority.compareTo(o.getPriority());
        }
    }

    private IBpmTaskQueryCondition queryConditions;

    private int offset;
    private int limit = -1;


    public BpmTaskQuery(Dialect hibernateDialect) {
        this.hibernateDialect = hibernateDialect;
    }

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

    public BpmTaskQuery excludeDefinitionIds(Collection<String> excludedDefinitionIds) {
        this.excludedDefinitionIds = excludedDefinitionIds;
        return this;
    }

    public BpmTaskQuery orderBy(String sortOrderColumnName, Integer priority, QueueOrder sortOrder)
    {
        SortingOrder sortingOrder = new SortingOrder();
        sortingOrder.setOrder(sortOrder);
        sortingOrder.setColumnName(sortOrderColumnName);
        sortingOrder.setPriority(priority);

        columnSorting.add(sortingOrder);

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
		Map<Integer, BpmTask> tasksById = new HashMap<Integer, BpmTask>();
		List<Integer> taskIdsWithoutAssignee = new ArrayList<Integer>();

        ProcessDefinitionDAO processDefinitionDAO = getThreadProcessToolContext().getProcessDefinitionDAO();

        for (Object[] resultRow : queryResults) {
            ProcessInstance processInstance = (ProcessInstance)resultRow[0];
            int taskId = (Integer)resultRow[1];
            String assignee = (String)resultRow[2];
            //String groupId = (String)resultRow[3];
            String taskName = (String)resultRow[4];
            Date createDate = (Date)resultRow[5];
            Date finishDate = (Date)resultRow[6];
            String status = (String)resultRow[7];
            Long definitionId = (Long)resultRow[8];
            Date taskDeadline = (Date)resultRow[9];
            String stepInfo = (String)resultRow[10];

			if (tasksById.get(taskId) == null) {
				BpmTaskBean bpmTask = new BpmTaskBean();

				bpmTask.setProcessInstance(processInstance);
				bpmTask.setExecutionId(processInstance.getInternalId());
				bpmTask.setInternalTaskId(String.valueOf(taskId));
				bpmTask.setAssignee(assignee);
				bpmTask.setTaskName(taskName);
				bpmTask.setCreateDate(createDate);
				bpmTask.setFinishDate(finishDate);
				bpmTask.setFinished(Status.valueOf(status) == Status.Completed);
				bpmTask.setProcessDefinition(processDefinitionDAO.getCachedDefinitionById(definitionId));
				bpmTask.setDeadlineDate(taskDeadline);
				bpmTask.setStepInfo(stepInfo);

				result.add(bpmTask);
				tasksById.put(taskId, bpmTask);

				if (assignee == null) {
					taskIdsWithoutAssignee.add(taskId);
				}
			}
        }

		fillPotentialOwners(taskIdsWithoutAssignee, tasksById);

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
                    .addScalar("createdOn", StandardBasicTypes.TIMESTAMP)
                    .addScalar("completedOn", StandardBasicTypes.TIMESTAMP)
                    .addScalar("taskStatus", StandardBasicTypes.STRING)
                    .addScalar("definitionId", StandardBasicTypes.LONG)
                    .addScalar("taskDeadline", StandardBasicTypes.TIMESTAMP)
                    .addScalar("stepInfo", StandardBasicTypes.STRING);
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
        StringBuilder sb = new StringBuilder(queryType == QueryType.COUNT ? 512 : 3 * 512);

        sb.append("SELECT ");

        if (queryType == QueryType.COUNT) {
            sb.append("COUNT(*)");
        }
        else {
            sb.append("process.*, task_.id as taskId, task_.actualowner_id as assignee, ");
            sb.append("NULL as groupId, ");
            sb.append("i18ntext_.shortText as taskName, task_.createdOn as createdOn, task_.completedOn as completedOn, ");
            sb.append("task_.status as taskStatus, process.definition_id as definitionId, ");
            sb.append(DEADLINE_SUBQUERY);
            sb.append("AS taskDeadline, stepInfo_.message AS stepInfo");
        }

        String castTypeName = hibernateDialect.getCastTypeName(Types.VARCHAR);
        sb.append(" FROM pt_process_instance process JOIN Task task_ ON CAST(task_.processinstanceid AS "+castTypeName+" ) = process.internalId ");
        sb.append(" LEFT JOIN PeopleAssignments_PotOwners potowners on potowners.task_id = task_.id AND potowners.entity_id = :user ");
        sb.append(" LEFT JOIN pt_process_instance_owners powner on powner.process_id = process.id AND powner.owners = :user ");

        queryParameters.add(new QueryParameter("user", user));

        if (taskNames != null || queryType == QueryType.LIST || hasText(searchExpression)) {
            sb.append(" JOIN I18NText i18ntext_ ON i18ntext_.task_names_id = task_.id");
        }

        if (excludedDefinitionIds != null && !excludedDefinitionIds.isEmpty()) {
            sb.append(" JOIN pt_process_definition_config def ON def.id = process.definition_id");
        }

        if (queryType == QueryType.LIST) {
            sb.append(" LEFT JOIN pt_step_info stepInfo_ ON stepInfo_.taskId = task_.id");
        }

        /* Add additional join conditions */
        for(SortingOrder sortingOrder: columnSorting)
            sb.append(queryConditions.getSortJoinCondition(sortingOrder.getColumnName()));

        sb.append(queryConditions.getJoin());

        sb.append(" WHERE 1=1");

        if (queues != null)
        {
            sb.append(" AND potowners.entity_id IN (:queues) ");
			sb.append(" AND task_.actualowner_id IS NULL");
			sb.append(" AND task_.status NOT IN ('Completed')");
            queryParameters.add(new QueryParameter("queues", queues));
        }

        if (owners != null) {
            sb.append(" AND owners IN (:owners)");
            queryParameters.add(new QueryParameter("owners", owners));
        }

        if (virtualQueues != null && user != null) {
            sb.append(from(virtualQueues).select(GET_VIRTUAL_QUEUES).toString(" OR ", " AND (", ")"));
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

        sb.append(queryConditions.getWhereCondition());

        if (hasText(searchExpression)) {
            sb.append(" AND (");
            sb.append("task_.actualowner_id LIKE '%' || :expression || '%'");
//            sb.append(" OR process.creatorLogin LIKE '%' || :expression || '%'");
//            sb.append(" OR (CASE WHEN process.externalKey IS NOT NULL THEN process.externalKey ELSE process.internalId END) LIKE '%' || :expression || '%'");
//            sb.append(" OR to_char(task_.createdOn, 'YYYY-MM-DD HH24:MI:SS') LIKE :expression || '%'");
//            sb.append(" OR EXISTS(SELECT 1 FROM pt_process_instance_s_attr attr");
//            sb.append("	WHERE attr.process_instance_id = process.id AND attr.value_ LIKE '%' || :expression || '%')");
//            sb.append(" OR to_char(");
//            sb.append(DEADLINE_SUBQUERY);
//            sb.append(", 'YYYY-MM-DD HH24:MI:SS') LIKE :expression || '%'");
            sb.append(" OR " + queryConditions.getSearchCondition());
//
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
                sb.append(" OR EXISTS(SELECT 1 FROM pt_process_state_config psc WHERE psc.id IN (:searchProcessStateIds)");
                sb.append(" AND psc.definition_id = process.definition_id AND psc.name = i18ntext_.shortText)");

                queryParameters.add(new QueryParameter("searchProcessStateIds", stateDescrKeys));
            }

            sb.append(')');
        }

        if (excludedDefinitionIds != null && !excludedDefinitionIds.isEmpty()) {
            sb.append(" AND (def.bpmDefinitionKey || '_' || def.bpmDefinitionVersion) NOT IN (:excludedDefinitionIds)");

            queryParameters.add(new QueryParameter("excludedDefinitionIds", excludedDefinitionIds));
        }

        if (queryType == QueryType.LIST && locale != null) {
            sb.append(" AND (stepInfo_.locale IS NULL OR stepInfo_.locale = :locale)");

            queryParameters.add(new QueryParameter("locale", locale.getLanguage()));
        }

        if (queryType == QueryType.LIST) {
            sb.append(getOrderCondition());
        }
        String txt = sb.toString();
//        log.info(txt);
        return txt;
    }

    private String getOrderCondition()
    {

        if (!columnSorting.isEmpty())
        {
            String orderString = " ORDER BY ";
            int moreConditions = columnSorting.size();
            Collections.sort(columnSorting);

            boolean sortCodnitionAdded = false;

            for(SortingOrder sortingOrder: columnSorting)
            {
                moreConditions--;

                String conditionSql = queryConditions.getSortQuery(sortingOrder.getColumnName());
                if(conditionSql == null || conditionSql.isEmpty())
                    continue;

                conditionSql += " " + (sortingOrder.getOrder() == QueueOrder.DESC ? " DESC" : " ASC");

                if(moreConditions > 0)
                    conditionSql += ", ";

                sortCodnitionAdded = true;

                orderString += conditionSql;
            }

            if(sortCodnitionAdded)
                return  orderString;
        }


        return " ORDER BY task_.createdOn DESC ";
    }

    public BpmTaskQuery queryConditions(IBpmTaskQueryCondition queryConditions) {
        this.queryConditions = queryConditions;
        return this;
    }

    private static final F<QueueType,Object> GET_VIRTUAL_QUEUES = new F<QueueType, Object>() {
        @Override
        public Object invoke(QueueType virtualQueue) {
            return getVirtualQueueCondition(virtualQueue);
        }
    };

    private static String getVirtualQueueCondition(QueueType virtualQueue) {
        switch (virtualQueue) {
            case ALL_TASKS:
                return "(((potowners.entity_id = :user AND task_.status NOT IN ('Reserved')) OR task_.actualowner_id = :user) AND task_.status NOT IN ('Completed'))";
            case MY_TASKS:
                return "(((potowners.entity_id = :user AND task_.status NOT IN ('Reserved')) OR task_.actualowner_id = :user) AND task_.status NOT IN ('Completed'))";
            case OWN_IN_PROGRESS:
                return "((process.creatorLogin = :user OR (owners IN (:user))) AND task_.status NOT IN ('Completed') AND (task_.actualowner_id != :user OR task_.actualowner_id is null))";
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

            if (localizedMessage != null && localizedMessage.toLowerCase(locale).contains(searchExpressionLC)) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

	private static final String GET_POTENTIAL_OWNERS =
			"SELECT po.task_id AS taskId, oe.dtype AS entityType, po.entity_id AS entityName \n" +
			"FROM PeopleAssignments_PotOwners po JOIN OrganizationalEntity oe ON oe.id = po.entity_id \n" +
			"WHERE po.task_id IN (:taskIds)";

	private void fillPotentialOwners(List<Integer> taskIdsWithoutAssignee, Map<Integer, BpmTask> tasksById) {
		if (taskIdsWithoutAssignee.isEmpty()) {
			return;
		}

		SQLQuery query = getThreadProcessToolContext().getHibernateSession().createSQLQuery(GET_POTENTIAL_OWNERS);

		query.addScalar("taskId", StandardBasicTypes.INTEGER)
				.addScalar("entityType", StandardBasicTypes.STRING)
				.addScalar("entityName", StandardBasicTypes.STRING)
				.setParameterList("taskIds", taskIdsWithoutAssignee);

		List<Object[]> list = query.list();

		for (Object[] row : list) {
			Integer taskId = (Integer)row[0];
			String entityType = (String)row[1];
			String entityName = (String)row[2];

			BpmTask task = tasksById.get(taskId);

			if ("User".equals(entityType)) {
				task.getPotentialOwners().add(entityName);
			}
			else if ("Group".equals(entityType)) {
				task.getQueues().add(entityName);
				((BpmTaskBean)task).setGroupId(entityName);
			}
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
                ", searchExpression='" + searchExpression + '\'' +
                ", offset=" + offset +
                ", limit=" + limit +
                '}';
    }
}
