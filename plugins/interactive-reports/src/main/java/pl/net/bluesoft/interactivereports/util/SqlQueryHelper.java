package pl.net.bluesoft.interactivereports.util;

import org.hibernate.SQLQuery;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;

import java.util.*;

import static pl.net.bluesoft.rnd.processtool.ProcessToolContext.Util.getThreadProcessToolContext;

/**
 * User: POlszewski
 * Date: 2014-06-27
 */
public class SqlQueryHelper {
	private final SQLQuery sqlQuery;

	public SqlQueryHelper(String query) {
		this.sqlQuery = getThreadProcessToolContext().getHibernateSession().createSQLQuery(query);
	}

	public SqlQueryHelper setNotNullParameterString(String name, String value) {
		return value != null ? setParameterString(name, value) : this;
	}

	public SqlQueryHelper setNotNullParameterTimestamp(String name, Date value) {
		return value != null ? setParameterTimestamp(name, value) : this;
	}

	public SqlQueryHelper setParameterString(String name, String value) {
		return setParameter(name, value, StandardBasicTypes.STRING);
	}

	public SqlQueryHelper setParameterTimestamp(String name, Date value) {
		return setParameter(name, value, StandardBasicTypes.TIMESTAMP);
	}

	private SqlQueryHelper setParameter(String name, Object value, Type type) {
		sqlQuery.setParameter(name, value, type);
		return this;
	}

	public SqlQueryHelper setParamLongList(String name, Collection<Long> vals) {
		sqlQuery.setParameterList(name, vals, StandardBasicTypes.LONG);
		return this;
	}

	public SqlQueryHelper setParamStringList(String name, Collection<String> vals) {
		sqlQuery.setParameterList(name, vals, StandardBasicTypes.STRING);
		return this;
	}

	public SqlQueryHelper addScalarLong(String columnAlias) {
		return addScalar(columnAlias, StandardBasicTypes.LONG);
	}

	public SqlQueryHelper addScalarString(String columnAlias) {
		return addScalar(columnAlias, StandardBasicTypes.STRING);
	}

	public SqlQueryHelper addScalarDate(String columnAlias) {
		return addScalar(columnAlias, StandardBasicTypes.TIMESTAMP);
	}

	public SqlQueryHelper addScalarDouble(String columnAlias) {
		return addScalar(columnAlias, StandardBasicTypes.DOUBLE);
	}


	private SqlQueryHelper addScalar(String columnAlias, Type type) {
		sqlQuery.addScalar(columnAlias, type);
		return this;
	}

	public interface RowMapper<ResultType> {
		ResultType mapRow(Object[] row);
	}

	public <ResultType> List<ResultType> list(RowMapper<ResultType> mapper) {
		List<Object[]> list = sqlQuery.list();

		if (list.isEmpty()) {
			return Collections.emptyList();
		}

		List<ResultType> result = new ArrayList<ResultType>(list.size());

		for (Object[] row : list) {
			result.add(mapper.mapRow(row));
		}
		return result;
	}
}
