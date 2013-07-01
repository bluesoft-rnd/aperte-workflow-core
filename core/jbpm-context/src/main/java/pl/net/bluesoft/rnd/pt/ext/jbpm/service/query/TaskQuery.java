package pl.net.bluesoft.rnd.pt.ext.jbpm.service.query;

import org.jbpm.task.Status;
import org.jbpm.task.TaskService;

import java.util.*;

import static pl.net.bluesoft.util.lang.cquery.CQuery.from;

/**
 * User: POlszewski
 * Date: 2013-06-14
 * Time: 15:32
 */
public class TaskQuery<T> extends QueryBase<T> {
	private Long processInstanceId;
	private long taskId;
	private String assignee;
	private boolean assigneeIsNull;
	private Collection<String> activityNames;
	private boolean active;
	private boolean completed;
	private boolean notSuspended;
	private Date createdBefore;
	private Date createdAfter;
	private Date completedAfter;
	private Collection<String> groupIds;

	private final List<String> projections = new ArrayList<String>();
	private final List<String> orders = new ArrayList<String>();
	private final List<String> groups = new ArrayList<String>();


	public TaskQuery(TaskService taskService) {
		super(taskService);
	}

	public TaskQuery<T> processInstanceId(long processInstanceId) {
		this.processInstanceId = processInstanceId;
		return this;
	}

	public TaskQuery<Object> selectGroupId() {
		projections.add("potentialOwner.id as groupId");
		return (TaskQuery<Object>)this;
	}

	public TaskQuery<Object> selectCount() {
		projections.add("count(*) as taskCount");
		return (TaskQuery<Object>)this;
	}

	public TaskQuery<T> taskId(long taskId) {
		this.taskId = taskId;
		return this;
	}

	public TaskQuery<T> assignee(String assignee) {
		this.assignee = assignee;
		return this;
	}

	public TaskQuery<T> assigneeIsNull() {
		this.assigneeIsNull = true;
		return this;
	}

	public TaskQuery<T> activityNames(Collection<String> activityNames) {
		this.activityNames = activityNames;
		return this;
	}

	public TaskQuery<T> activityName(String activityName) {
		this.activityNames = activityName != null ? Collections.singleton(activityName) : null;
		return this;
	}

	public TaskQuery<T> completed() {
		this.completed = true;
		return this;
	}

	public TaskQuery<T> notSuspended() {
		this.notSuspended = true;
		return this;
	}

	public TaskQuery<T> active() {
		this.active = true;
		return this;
	}

	public TaskQuery<T> groupId(String name) {
		return groupIds(name != null ? Collections.singleton(name) : null);
	}

	public TaskQuery<T> groupIds(Collection<String> names) {
		this.groupIds = names;
		return this;
	}

	public TaskQuery<T> createdBefore(Date date) {
		this.createdBefore = date;
		return this;
	}

	public TaskQuery<T> createdAfter(Date date) {
		this.createdAfter = date;
		return this;
	}

	public TaskQuery<T> completedAfter(Date date) {
		this.completedAfter = date;
		return this;
	}

	public TaskQuery<T> groupByGroupId() {
		groups.add("potentialOwner.id");
		return this;
	}

	public TaskQuery<T> orderByTaskIdDesc() {
		orders.add("task.id DESC");
		return this;
	}

	public TaskQuery<T> orderByCompleteDate() {
		orders.add("task.taskData.completedOn");
		return this;
	}

	public TaskQuery<T> orderByCompleteDateDesc() {
		orders.add("task.taskData.completedOn DESC");
		return this;
	}

	public TaskQuery<T> orderByCreateDate() {
		orders.add("task.taskData.createdOn");
		return this;
	}

	public int count() {
		return (Integer)selectCount().first();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("SELECT ");

		if (projections.isEmpty()) {
			sb.append("task");
		}
		else {
			sb.append(from(projections).toString(","));
		}

		sb.append(" FROM org.jbpm.task.Task task");

		if (activityNames != null) {
			sb.append(" LEFT JOIN FETCH task.names name");
		}

		if (groupIds != null) {
			if (groups.isEmpty()) {
				sb.append(" LEFT JOIN FETCH task.peopleAssignments.potentialOwners potentialOwner");
			}
			else {
				sb.append(" LEFT JOIN task.peopleAssignments.potentialOwners potentialOwner");
			}
		}

		sb.append(" WHERE 1=1");

		if (processInstanceId != null) {
			sb.append(" AND task.taskData.processInstanceId = ").append(processInstanceId);
		}

		if (taskId > 0) {
			sb.append(" AND task.id = ").append(taskId);
		}

		if (assignee != null) {
			sb.append(" AND task.taskData.actualOwner = '").append(escapeHql(assignee)).append('\'');
		}

		if (assigneeIsNull) {
			sb.append(" AND task.taskData.actualOwner.id IS NULL ");
		}

		if (activityNames != null) {
			sb.append(" AND name.shortText in ").append(strList(activityNames));
		}

		if (completed) {
			sb.append(" AND task.taskData.status IN ").append(strList(Status.Completed));
		}

		if (active) {
			sb.append(" AND task.taskData.status NOT IN ")
				.append(strList(Status.Error, Status.Exited, Status.Failed, Status.Completed));
		}

		if (notSuspended) {
			sb.append(" AND task.taskData.status NOT IN ").append(strList(Status.Suspended));
		}

		if (createdBefore != null) {
			sb.append(" AND task.taskData.createdOn < '").append(toDate(createdBefore)).append('\'');
		}

		if (createdAfter != null) {
			sb.append(" AND task.taskData.createdOn > '").append(toDate(createdAfter)).append('\'');
		}

		if (completedAfter != null) {
			sb.append(" AND task.taskData.completedOn > '").append(toDate(completedAfter)).append('\'');
		}

		if (groupIds != null) {
			sb.append(" AND task.taskData.actualOwner.id IS NULL ")
					.append(" AND potentialOwner.id IN ")
					.append(strList(groupIds));
		}

		if (!groups.isEmpty()) {
			sb.append(" GROUP BY ").append(from(groups).toString(","));
		}

		if (!orders.isEmpty()) {
			sb.append(" ORDER BY ").append(from(orders).toString(","));
		}
		return sb.toString();
	}
}
