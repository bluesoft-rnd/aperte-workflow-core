package pl.net.bluesoft.rnd.pt.ext.jbpm.service.query;

import org.jbpm.task.TaskService;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static pl.net.bluesoft.util.lang.cquery.CQuery.from;

/**
 * User: POlszewski
 * Date: 2013-06-24
 * Time: 11:14
 */
public abstract class QueryBase<T> {
	private final TaskService taskService;

	private int offset = 0;
	private Integer limit = Integer.MAX_VALUE;


	protected QueryBase(TaskService taskService) {
		this.taskService = taskService;
	}

	public abstract String toString();

	public QueryBase<T> page(int offset, int limit) {
		this.offset = offset;
		this.limit = limit;
		return this;
	}

	public T first() {
		List<T> list = page(0, 1).list();
		return list.isEmpty() ? null : list.get(0);
	}

	public List<T> list() {
		String hql = toString();

		return (List<T>)taskService.query(hql, limit, offset);
	}

	protected static <T> String strList(T... elems) {
		return strList(Arrays.asList(elems));
	}

	protected static <T> String strList(Collection<T> elems) {
		if (elems.isEmpty()) {
			throw new IllegalArgumentException("List is empty");
		}
		return from(elems).toString("','", "('", "')");
	}

	protected static String toDate(Date date) {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
	}

	protected static String escapeHql(String hql) {
		return hql;
	}

	protected static void todo() {
		throw new RuntimeException("TODO");
	}
}
