package pl.net.bluesoft.rnd.pt.ext.jbpm.service.query;

import org.hibernate.SQLQuery;
import org.hibernate.dialect.Dialect;
import org.hibernate.type.StandardBasicTypes;
import pl.net.bluesoft.rnd.processtool.BasicSettings;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.BpmTaskNotification;
import pl.net.bluesoft.rnd.processtool.dao.ProcessDefinitionDAO;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.i18n.I18NSourceFactory;

import java.sql.Types;
import java.util.*;

import static pl.net.bluesoft.rnd.processtool.ProcessToolContext.Util.getThreadProcessToolContext;
import static pl.net.bluesoft.util.lang.Strings.withEnding;

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

    private Dialect hibernateDialect;

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

        if (queryResults.isEmpty()) {
            return result;
        }

        ProcessToolContext ctx = getThreadProcessToolContext();
        ProcessDefinitionDAO processDefinitionDAO = ctx.getProcessDefinitionDAO();
        I18NSource i18NSource = I18NSourceFactory.createI18NSource(locale);
        String activityPortletUrl = ctx.getSetting(BasicSettings.ACTIVITY_PORTLET_URL);

        for (Object[] resultRow : queryResults) {
            Long taskId = (Long)resultRow[0];
            String taskName = (String)resultRow[1];
            Long processDefinitionId = (Long)resultRow[2];
            Date creationDate = (Date)resultRow[3];
            Date completionDate = (Date)resultRow[4];

			BpmTaskNotification notification = new BpmTaskNotification();

			notification.setTaskId(taskId);
			if (completionDate == null) {
				ProcessDefinitionConfig definition = processDefinitionDAO.getCachedDefinitionById(processDefinitionId);
				String description = definition.getProcessStateConfigurationByName(taskName).getDescription();

				notification.setDescription(i18NSource.getMessage(description));
				if (activityPortletUrl != null) {
					notification.setLink(withEnding(activityPortletUrl, "/") + "?taskId=" + taskId);
				}
			}
			notification.setCreationDate(creationDate);
			notification.setCompletionDate(completionDate);

			result.add(notification);
		}
		fetchAdditionalDescription(result, locale);
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
        String castTypeName = hibernateDialect.getCastTypeName(Types.VARCHAR);

        sb.append("SELECT task_.id as taskId, \t\n" +
                " i18ntext_.shorttext AS taskName,\n" +
                " process.definition_id AS processDefinitionId,\n" +
                " task_.createdOn AS creationDate,\n" +
                " task_.completedOn AS completionDate\n" +
                "FROM \n" +
                " pt_process_instance process \n" +
                " JOIN task task_ ON CAST(task_.processinstanceid AS "+castTypeName+") = process.internalId\n" +
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

	private void fetchAdditionalDescription(List<BpmTaskNotification> result, Locale locale) {
		List<Long> taskIds = getTaskInProgressIds(result);

		if (taskIds.isEmpty()) {
			return;
		}

		SQLQuery query = getFetchAdditionalDescriptionQuery(taskIds);

		List<Object[]> queryResults = query.list();

		Map<Long, Map<String, String>> descrByTaskIdByLocale = groupByTaskIdByLocale(queryResults);

		for (BpmTaskNotification notification : result) {
			String additionalDescr = getAdditionalDescr(descrByTaskIdByLocale, notification.getTaskId(), locale);

			notification.setAdditionalDescription(additionalDescr);
		}
	}

	private String getAdditionalDescr(Map<Long, Map<String, String>> descrByTaskIdByLocale, Long taskId, Locale locale) {
		Map<String, String> byLocale = descrByTaskIdByLocale.get(taskId);

		if (byLocale != null) {
			String result = byLocale.get(locale.getCountry()); // specific language

			if (result == null) { // default language
				result = byLocale.get(null);
			}
			if (result == null) { // any
				result = byLocale.values().iterator().next();
			}
			return result;
		}
		return null;
	}

	private Map<Long, Map<String, String>> groupByTaskIdByLocale(List<Object[]> queryResults) {
		Map<Long, Map<String, String>> result = new HashMap<Long, Map<String, String>>();

		for (Object[] resultRow : queryResults) {
			Long taskId = (Long)resultRow[0];
			String locale = (String)resultRow[1];
			String message = (String)resultRow[2];

			Map<String, String> byLocale = result.get(taskId);

			if (byLocale == null) {
				byLocale = new HashMap<String, String>();
				result.put(taskId, byLocale);
			}
			byLocale.put(locale, message);
		}
		return result;
	}

	private SQLQuery getFetchAdditionalDescriptionQuery(List<Long> taskIds) {
		String queryString = "SELECT taskId, locale, message FROM pt_step_info WHERE taskId IN (:taskIds)";

		SQLQuery query = getThreadProcessToolContext().getHibernateSession().createSQLQuery(queryString);

		query.addScalar("taskId", StandardBasicTypes.LONG)
				.addScalar("locale", StandardBasicTypes.STRING)
				.addScalar("message", StandardBasicTypes.STRING);

		query.setParameterList("taskIds", taskIds);

		return query;
	}

	private List<Long> getTaskInProgressIds(List<BpmTaskNotification> list) {
		if (list.isEmpty()) {
			return Collections.emptyList();
		}

		List<Long> result = new ArrayList<Long>();

		for (BpmTaskNotification notification : list) {
			if (notification.getCompletionDate() == null) {
				result.add(notification.getTaskId());
			}
		}
		return result;
	}

    public void setHibernateDialect(Dialect hibernateDialect) {
        this.hibernateDialect = hibernateDialect;
    }
}
