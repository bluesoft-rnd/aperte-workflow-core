package pl.net.bluesoft.rnd.pt.ext.jbpm.service.query;

import org.hibernate.SQLQuery;
import org.hibernate.type.StandardBasicTypes;
import pl.net.bluesoft.rnd.processtool.bpm.BpmTaskNotification;
import pl.net.bluesoft.rnd.processtool.dao.ProcessDefinitionDAO;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.i18n.I18NSourceFactory;

import java.util.*;

import static pl.net.bluesoft.rnd.processtool.ProcessToolContext.Util.getThreadProcessToolContext;

/**
 * User: POlszewski
 * Date: 2014-01-27
 */
public class BpmTaskNotificationQuery {
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

	private String user;
	private Date date;

	private int offset;
	private int limit = -1;

	public BpmTaskNotificationQuery user(String user) {
		this.user = user;
		return this;
	}

	public BpmTaskNotificationQuery date(Date date) {
		this.date = date;
		return this;
	}

	public BpmTaskNotificationQuery page(int offset, int limit) {
		this.offset = offset;
		this.limit = limit;
		return this;
	}

	public List<BpmTaskNotification> list(Locale locale) {
		SQLQuery query = getQuery();
		List<Object[]> queryResults = query.list();

		List<BpmTaskNotification> result = new ArrayList<BpmTaskNotification>();

		ProcessDefinitionDAO processDefinitionDAO = getThreadProcessToolContext().getProcessDefinitionDAO();
		I18NSource i18NSource = I18NSourceFactory.createI18NSource(locale);

		for (Object[] resultRow : queryResults) {
			Long taskId = (Long)resultRow[0];
			String taskName = (String)resultRow[1];
			Long processDefinitionId = (Long)resultRow[2];
			Date creationDate = (Date)resultRow[3];
			Date completionDate = (Date)resultRow[4];

			ProcessDefinitionConfig definition = processDefinitionDAO.getCachedDefinitionById(processDefinitionId);
			String description = definition.getProcessStateConfigurationByName(taskName).getDescription();

			BpmTaskNotification notification = new BpmTaskNotification();

			notification.setTaskId(taskId);
			if (completionDate == null) {
				notification.setDescription(i18NSource.getMessage(description));
				notification.setLink("http://localhost:8080/?taskId=" + taskId);//TODO
			}
			notification.setCreationDate(creationDate);
			notification.setCompletionDate(completionDate);

			result.add(notification);
		}
		return result;
	}

	private SQLQuery getQuery() {
		List<QueryParameter> queryParameters = new ArrayList<QueryParameter>();
		String queryString = getQueryString(queryParameters);
		SQLQuery query = getThreadProcessToolContext().getHibernateSession().createSQLQuery(queryString);

		query.addScalar("taskId", StandardBasicTypes.LONG)
				.addScalar("taskName", StandardBasicTypes.STRING)
				.addScalar("processDefinitionId", StandardBasicTypes.LONG)
				.addScalar("creationDate", StandardBasicTypes.TIMESTAMP)
				.addScalar("completionDate", StandardBasicTypes.TIMESTAMP);

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

	private String getQueryString(List<QueryParameter> queryParameters) {
		StringBuilder sb = new StringBuilder(3 * 512);

		sb.append("SELECT task_.id as taskId, \t\n" +
				" i18ntext_.shorttext AS taskName,\n" +
				" process.definition_id AS processDefinitionId,\n" +
				" task_.createdOn AS creationDate,\n" +
				" task_.completedOn AS completionDate\n" +
				"FROM \n" +
				" pt_process_instance process \n" +
				" JOIN task task_ ON CAST(task_.processinstanceid AS VARCHAR(10)) = process.internalId\n" +
				" JOIN i18ntext i18ntext_ ON i18ntext_.task_names_id = task_.id\n" +
				"WHERE 1=1\n");


		if (user != null) {
			sb.append(" AND task_.actualOwner_id = :user\n");
			queryParameters.add(new QueryParameter("user", user));
		}

		if (date != null) {
			sb.append(" AND COALESCE(task_.completedOn, task_.createdOn) > :date_\n");
			queryParameters.add(new QueryParameter("date_", date));
		}

		return sb.toString();
	}
}
