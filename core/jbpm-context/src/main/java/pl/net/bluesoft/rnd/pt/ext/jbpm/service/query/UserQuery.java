package pl.net.bluesoft.rnd.pt.ext.jbpm.service.query;

import org.jbpm.task.TaskService;

/**
 * User: POlszewski
 * Date: 2013-06-14
 * Time: 16:22
 */
public class UserQuery<T> extends QueryBase<T> {
	private boolean selectId;
	private String userIdPattern;

	public UserQuery(TaskService taskService) {
		super(taskService);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("SELECT ");

		if (selectId) {
			sb.append("user.id");
		}
		else {
			sb.append("user");
		}

		sb.append(" FROM org.jbpm.task.User user WHERE 1=1");

		if (userIdPattern != null) {
			sb.append(" AND user.id LIKE '").append(escapeHql(userIdPattern)).append('\'');
		}

		return sb.toString();
	}

	public UserQuery<String> selectId() {
		selectId = true;
		return (UserQuery<String>)this;
	}

	public UserQuery<T> whereIdLike(String pattern) {
		this.userIdPattern = pattern;
		return this;
	}
}
