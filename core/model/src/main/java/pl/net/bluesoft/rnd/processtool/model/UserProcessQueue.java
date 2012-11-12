package pl.net.bluesoft.rnd.processtool.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

/**
 * The entity represents one process instance which is attached 
 * to specified user's queue. The entity is being used during
 * process status change (assignee change, new subprocess 
 * creation) for the optymalization 
 * 
 * @author Maciej Pawlak
 *
 */
@Entity
@Table(name="pt_user_process_queue")

@org.hibernate.annotations.Table(appliesTo="pt_user_process_queue", indexes={
		@Index(name = "idx_user_login_queue_type",columnNames={"user_login", "queue_type"})})

public class UserProcessQueue extends PersistentEntity
{
	/** User login as string */
	@Column(name="user_login")
	private String login;

	/** Type of the queue */
	@Column(name="queue_type")
	@Enumerated(EnumType.STRING)
	private QueueType queueType;
	
	/** Process instance id */
	@Column(name="process_id")
	private Long processId;
	
	/** Task id */
	@Index(name="idx_task_id")
	@Column(name="task_id")
	private Long taskId;
	
	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public QueueType getQueueType() {
		return queueType;
	}

	public void setQueueType(QueueType queueType) {
		this.queueType = queueType;
	}

	public Long getProcessId()
	{
		return processId;
	}

	public void setProcessId(Long processId)
	{
		this.processId = processId;
	}

	public Long getTaskId() {
		return taskId;
	}

	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((taskId == null) ? 0 : taskId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserProcessQueue other = (UserProcessQueue) obj;
		if (taskId == null) {
			if (other.taskId != null)
				return false;
		} else if (!taskId.equals(other.taskId))
			return false;
		return true;
	}
}
